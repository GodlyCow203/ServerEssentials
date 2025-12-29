package net.godlycow.org.auction.gui;

import net.godlycow.org.auction.model.AuctionItem;
import net.godlycow.org.auction.storage.AuctionStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.godlycow.org.commands.config.AuctionConfig;
import net.godlycow.org.economy.eco.EconomyManager;
import net.godlycow.org.economy.eco.EconomyResponse;
import net.godlycow.org.language.PlayerLanguageManager;
import net.godlycow.org.language.LanguageManager.ComponentPlaceholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class AuctionGUIListener implements Listener {
    private final Plugin plugin;
    private final PlayerLanguageManager langManager;
    private final AuctionConfig config;
    private final AuctionStorage storage;
    private final EconomyManager economyManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private final Map<Player, Map<Integer, UUID>> auctionViewingMap = new WeakHashMap<>();
    private final Map<Player, Map<Integer, UUID>> playerItemsViewingMap = new WeakHashMap<>();
    private final Map<UUID, UUID> pendingRemovalMap = new HashMap<>();

    public AuctionGUIListener(Plugin plugin, PlayerLanguageManager langManager,
                              AuctionConfig config, AuctionStorage storage, EconomyManager economyManager) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.config = config;
        this.storage = storage;
        this.economyManager = economyManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();
        if (!title.contains("Auction House") && !title.contains("Your Items") &&
                !title.contains("Confirm Removal")) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        if (title.contains("Confirm Removal")) {
            handleConfirmRemoval(player, clicked, event.getRawSlot());
            return;
        }

        if (handleNavigation(player, clicked, title)) return;

        if (title.contains("Your Items")) {
            handlePlayerItemClick(player, event.getRawSlot());
        } else if (title.contains("Auction House")) {
            handleAuctionItemClick(player, event.getRawSlot());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        auctionViewingMap.remove(player);
        playerItemsViewingMap.remove(player);
        pendingRemovalMap.remove(player);
    }

    private void handleConfirmRemoval(Player player, ItemStack clicked, int slot) {
        UUID itemId = pendingRemovalMap.remove(player.getUniqueId());

        if (itemId == null) {
            plugin.getLogger().warning("[Auction] No pending removal for " + player.getName());
            player.sendMessage(langManager.getMessageFor(
                    player,
                    "auction.remove.no-pending",
                    "<red>No pending removal found!"
            ));
            return;
        }

        if (slot == 11 && clicked.getType() == Material.GREEN_WOOL) {
            plugin.getLogger().fine("[Auction] Removing item " + itemId);

            storage.getItemData(itemId).thenAccept(optItem -> {
                if (optItem.isEmpty()) return;

                AuctionItem item = optItem.get();
                storage.removeItem(itemId).thenRun(() -> {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        HashMap<Integer, ItemStack> remaining =
                                player.getInventory().addItem(item.getItem().clone());

                        if (!remaining.isEmpty()) {
                            remaining.values().forEach(drop ->
                                    player.getWorld().dropItemNaturally(player.getLocation(), drop)
                            );

                            player.sendMessage(langManager.getMessageFor(
                                    player,
                                    "auction.remove.inventory-full",
                                    "<yellow>Item dropped on the ground (inventory full)"
                            ));
                        }

                        player.sendMessage(langManager.getMessageFor(
                                player,
                                "auction.remove.success",
                                "<green>Item removed successfully!"
                        ));

                        openPlayerItemsGUI(player, 1);
                    });
                });
            });

        } else if (slot == 15 && clicked.getType() == Material.RED_WOOL) {
            player.sendMessage(langManager.getMessageFor(
                    player,
                    "auction.remove.cancelled",
                    "<gray>Removal cancelled."
            ));
            openPlayerItemsGUI(player, 1);
        }
    }


    private boolean handleNavigation(Player player, ItemStack clicked, String title) {
        return switch (clicked.getType()) {
            case BARRIER -> {
                player.closeInventory();
                yield true;
            }
            case ARROW -> {
                int page = extractPage(title);
                int newPage = clicked.getItemMeta().getDisplayName().contains("Next") ?
                        page + 1 : Math.max(page - 1, 1);

                if (title.contains("Your Items")) {
                    openPlayerItemsGUI(player, newPage);
                } else {
                    openAuctionGUI(player, newPage);
                }
                yield true;
            }
            case PAPER -> {
                if (title.contains("Auction House")) {
                    int page = extractPage(title);
                    openAuctionGUI(player, page);
                    player.sendMessage(langManager.getMessageFor(player, "auction.gui.refresh",
                            "<green>Auction refreshed!"));
                }
                yield true;
            }
            case CHEST -> {
                if (title.contains("Auction House")) {
                    openPlayerItemsGUI(player, 1);
                }
                yield true;
            }
            case PLAYER_HEAD -> {
                if (title.contains("Auction House")) {
                    double balance = economyManager.getBalance(player);
                    player.sendMessage(langManager.getMessageFor(player, "auction.gui.balance",
                            "<green>Balance: <yellow>${balance}",
                            ComponentPlaceholder.of("{balance}", economyManager.format(balance))));
                }
                yield true;
            }
            default -> false;
        };
    }

    private void handlePlayerItemClick(Player player, int slot) {
        Map<Integer, UUID> slotMap = playerItemsViewingMap.get(player);
        if (slotMap == null) return;

        UUID itemId = slotMap.get(slot);
        if (itemId == null) return;

        pendingRemovalMap.put(player.getUniqueId(), itemId);
        openRemoveConfirmGUI(player);
    }

    private void handleAuctionItemClick(Player player, int slot) {
        Map<Integer, UUID> slotMap = auctionViewingMap.get(player);
        if (slotMap == null) return;

        UUID itemId = slotMap.get(slot);
        if (itemId == null) return;

        storage.getAllActiveItems().thenAccept(items -> {
            AuctionItem item = items.stream()
                    .filter(i -> i.getId().equals(itemId))
                    .findFirst()
                    .orElse(null);

            if (item == null) {
                player.sendMessage(langManager.getMessageFor(player, "auction.purchase.not-found",
                        "<red>Item no longer available!"));
                return;
            }

            processPurchase(player, item);
        });
    }

    private void processPurchase(Player player, AuctionItem item) {
        if (!economyManager.isEnabled()) {
            player.sendMessage(langManager.getMessageFor(player, "auction.purchase.no-economy",
                    "<red>§c✗ Economy system is not available."));
            return;
        }

        double balance = economyManager.getBalance(player);
        if (balance < item.getPrice()) {
            player.sendMessage(langManager.getMessageFor(player, "auction.purchase.not-enough-money",
                    "<red>You don't have enough money!"));
            return;
        }

        CompletableFuture.runAsync(() -> {
            EconomyResponse withdrawResponse = economyManager.withdraw(player, item.getPrice());

            if (!withdrawResponse.success()) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage(langManager.getMessageFor(player, "auction.purchase.failed",
                            "<red>Transaction failed: " + withdrawResponse.errorMessage));
                });
                return;
            }

            Player sellerPlayer = Bukkit.getPlayer(item.getSeller());
            if (sellerPlayer != null && sellerPlayer.isOnline()) {
                EconomyResponse depositResponse = economyManager.deposit(sellerPlayer, item.getPrice());
                if (!depositResponse.success()) {
                    economyManager.deposit(player, item.getPrice());
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        player.sendMessage(langManager.getMessageFor(player, "auction.purchase.failed",
                                "<red>Transaction failed: Could not pay seller"));
                    });
                    return;
                }

                sellerPlayer.sendMessage(langManager.getMessageFor(sellerPlayer, "auction.purchase.seller-message",
                        "<green>{buyer} purchased your item for ${price}!",
                        ComponentPlaceholder.of("{buyer}", player.getName()),
                        ComponentPlaceholder.of("{price}", economyManager.format(item.getPrice()))));
            }

            storage.removeItem(item.getId()).thenAccept(v -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.getInventory().addItem(item.getItem().clone());

                    player.sendMessage(langManager.getMessageFor(player, "auction.purchase.success",
                            "<green>Purchased for <yellow>${price}</yellow>!",
                            ComponentPlaceholder.of("{price}", economyManager.format(item.getPrice()))));

                    int currentPage = extractPage(player.getOpenInventory().getTitle());
                    openAuctionGUI(player, currentPage);
                });
            }).exceptionally(ex -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage(langManager.getMessageFor(player, "auction.purchase.failed",
                            "<red>Failed to complete purchase. Please try again."));
                });
                plugin.getLogger().severe("[Auction] Failed to remove item after purchase: " + ex.getMessage());
                return null;
            });
        });
    }

    private int extractPage(String title) {
        if (!title.contains("Page ")) return 1;
        String[] parts = title.split("Page ");
        if (parts.length < 2) return 1;
        String pageStr = parts[1].split(" ")[0].replace(")", "");
        try {
            return Integer.parseInt(pageStr);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    public void openAuctionGUI(Player player, int page) {
        if (!economyManager.isEnabled()) {
            player.sendMessage(langManager.getMessageFor(player, "auction.no-economy",
                    "<red>§c✗ Economy system is not available."));
            return;
        }
        storage.getAllActiveItems().thenAccept(items -> {
            Bukkit.getScheduler().runTask(plugin, () -> openAuctionGUISync(player, page, items));
        });
    }

    private void openAuctionGUISync(Player player, int page, List<AuctionItem> items) {
        items = filterExpired(items);
        int totalPages = Math.max(1, (int) Math.ceil((double) items.size() / config.itemsPerPage));
        page = Math.min(page, totalPages);

        Component title = langManager.getMessageFor(null, "auction.gui.titles.auction",
                config.guiTitle,
                ComponentPlaceholder.of("{page}", page));

        Inventory inv = Bukkit.createInventory(null, config.guiRows * 9, title);
        addBorder(inv);

        Map<Integer, UUID> slotMap = new HashMap<>();
        int start = (page - 1) * config.itemsPerPage;
        int end = Math.min(start + config.itemsPerPage, items.size());

        for (int i = start; i < end; i++) {
            AuctionItem item = items.get(i);
            int slot = getSlot(i - start);
            slotMap.put(slot, item.getId());
            inv.setItem(slot, createDisplayItem(item, false));
        }

        auctionViewingMap.put(player, slotMap);

        inv.setItem(45, createGuiItem(Material.ARROW, "auction.gui.navigation.prev"));
        inv.setItem(0, createGuiItem(Material.PAPER, "auction.gui.navigation.refresh"));
        inv.setItem(4, createPlayerBalanceItem(player));
        inv.setItem(8, createGuiItem(Material.CHEST, "auction.gui.navigation.my-items"));
        inv.setItem(49, createGuiItem(Material.BARRIER, "auction.gui.navigation.close"));
        inv.setItem(53, createGuiItem(Material.ARROW, "auction.gui.navigation.next"));

        player.openInventory(inv);
    }

    public void openPlayerItemsGUI(Player player, int page) {
        if (!economyManager.isEnabled()) {
            player.sendMessage(langManager.getMessageFor(player, "auction.no-economy",
                    "<red>§c✗ Economy system is not available."));
            return;
        }
        storage.getPlayerItems(player.getUniqueId()).thenAccept(items -> {
            Bukkit.getScheduler().runTask(plugin, () -> openPlayerItemsGUISync(player, page, items));
        });
    }

    private void openPlayerItemsGUISync(Player player, int page, List<AuctionItem> items) {
        items = filterExpired(items);
        int totalPages = Math.max(1, (int) Math.ceil((double) items.size() / config.itemsPerPage));
        page = Math.min(page, totalPages);

        Component title = langManager.getMessageFor(null, "auction.gui.titles.my-items",
                config.myItemsTitle,
                ComponentPlaceholder.of("{page}", page));

        Inventory inv = Bukkit.createInventory(null, config.guiRows * 9, title);
        addBorder(inv);

        Map<Integer, UUID> slotMap = new HashMap<>();
        int start = (page - 1) * config.itemsPerPage;
        int end = Math.min(start + config.itemsPerPage, items.size());

        for (int i = start; i < end; i++) {
            AuctionItem item = items.get(i);
            int slot = getSlot(i - start);
            slotMap.put(slot, item.getId());
            inv.setItem(slot, createDisplayItem(item, true));
        }

        playerItemsViewingMap.put(player, slotMap);

        inv.setItem(45, createGuiItem(Material.ARROW, "auction.gui.navigation.prev"));
        inv.setItem(53, createGuiItem(Material.ARROW, "auction.gui.navigation.next"));
        inv.setItem(49, createGuiItem(Material.BARRIER, "auction.gui.navigation.close"));

        player.openInventory(inv);
    }

    private void openRemoveConfirmGUI(Player player) {
        Component title = langManager.getMessageFor(null, "auction.gui.titles.remove-confirm",
                config.removeConfirmTitle);

        Inventory inv = Bukkit.createInventory(null, 27, title);
        inv.setItem(11, createGuiItem(Material.GREEN_WOOL, "auction.gui.remove.confirm"));
        inv.setItem(15, createGuiItem(Material.RED_WOOL, "auction.gui.remove.cancel"));

        player.openInventory(inv);
    }

    private ItemStack createDisplayItem(AuctionItem item, boolean forSeller) {
        ItemStack display = item.getItem().clone();
        ItemMeta meta = display.getItemMeta();

        meta.displayName(langManager.getMessageFor(null, "auction.gui.item.name",
                "<green>Price: <yellow>${price}",
                ComponentPlaceholder.of("{price}", economyManager.format(item.getPrice()))));

        List<Component> lore = new ArrayList<>();

        if (forSeller) {
            lore.add(langManager.getMessageFor(null, "auction.gui.item.lore.seller-price",
                    "<gray>Your price: <yellow>${price}",
                    ComponentPlaceholder.of("{price}", economyManager.format(item.getPrice()))));
        } else {
            lore.add(langManager.getMessageFor(null, "auction.gui.item.lore.seller",
                    "<gray>Seller: <yellow>{seller}",
                    ComponentPlaceholder.of("{seller}", getSellerName(item.getSeller()))));
        }

        lore.add(langManager.getMessageFor(null, "auction.gui.item.lore.expires",
                "<gray>Expires: <yellow>{date}",
                ComponentPlaceholder.of("{date}", formatDate(item.getExpiration()))));

        lore.add(langManager.getMessageFor(null, forSeller ? "auction.gui.item.lore.seller-click" : "auction.gui.item.lore.buyer-click",
                forSeller ? "<yellow>Click to remove" : "<yellow>Click to purchase"));

        meta.lore(lore);
        display.setItemMeta(meta);
        return display;
    }

    private ItemStack createGuiItem(Material material, String messageKey) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        Component nameComponent = langManager.getMessageFor(null, messageKey, "<white>Navigation");
        meta.displayName(nameComponent);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createPlayerBalanceItem(Player player) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        double balance = economyManager.getBalance(player);

        Component nameComponent = langManager.getMessageFor(player, "auction.gui.balance",
                "<green>Balance: <yellow>${balance}",
                ComponentPlaceholder.of("{balance}", economyManager.format(balance)));
        meta.displayName(nameComponent);

        skull.setItemMeta(meta);
        return skull;
    }

    private void addBorder(Inventory inv) {
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.displayName(Component.empty());
        glass.setItemMeta(meta);

        int size = inv.getSize();
        for (int i = 0; i < 9; i++) inv.setItem(i, glass);
        for (int i = size - 9; i < size; i++) inv.setItem(i, glass);
        for (int i = 9; i < size - 9; i += 9) {
            inv.setItem(i, glass);
            inv.setItem(i + 8, glass);
        }
    }

    private int getSlot(int index) {
        return (index / 7 + 1) * 9 + (index % 7) + 1;
    }

    private String getSellerName(UUID sellerId) {
        return Optional.ofNullable(Bukkit.getOfflinePlayer(sellerId).getName()).orElse("Unknown");
    }

    private String formatDate(long timestamp) {
        return LocalDate.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private List<AuctionItem> filterExpired(List<AuctionItem> items) {
        long now = System.currentTimeMillis();
        return items.stream().filter(item -> item.getExpiration() > now).toList();
    }
}