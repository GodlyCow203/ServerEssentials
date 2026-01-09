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
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class AuctionGUIListener implements Listener {
    private final Plugin plugin;
    private final PlayerLanguageManager langManager;
    private final AuctionConfig config;
    private final AuctionStorage storage;
    private final EconomyManager economyManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<UUID, Map<Integer, UUID>> auctionViewingMap = new ConcurrentHashMap<>();
    private final Map<UUID, Map<Integer, UUID>> playerItemsViewingMap = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> pendingRemovalMap = new ConcurrentHashMap<>();
    private final Map<UUID, GUIType> playerGUITypeMap = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> playerPageMap = new ConcurrentHashMap<>();
    private final Map<UUID, Long> clickCooldownMap = new ConcurrentHashMap<>();

    private static final long CLICK_COOLDOWN_MS = 300;

    private final NamespacedKey guiItemKey;
    private final NamespacedKey guiItemTypeKey;

    public enum GUIType {
        AUCTION_HOUSE, PLAYER_ITEMS, REMOVE_CONFIRM
    }

    public enum GuiItemType {
        PREVIOUS_PAGE, NEXT_PAGE, REFRESH, MY_ITEMS, CLOSE, BALANCE_INFO,
        CONFIRM_REMOVE, CANCEL_REMOVE, BORDER
    }

    public static class AuctionGUIHolder implements InventoryHolder {
        private final GUIType guiType;
        private final int page;

        public AuctionGUIHolder(GUIType guiType, int page) {
            this.guiType = guiType;
            this.page = page;
        }

        public GUIType getGuiType() {
            return guiType;
        }

        public int getPage() {
            return page;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    public AuctionGUIListener(Plugin plugin, PlayerLanguageManager langManager,
                              AuctionConfig config, AuctionStorage storage, EconomyManager economyManager) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.config = config;
        this.storage = storage;
        this.economyManager = economyManager;
        this.guiItemKey = new NamespacedKey(plugin, "auction_gui_item");
        this.guiItemTypeKey = new NamespacedKey(plugin, "auction_gui_item_type");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        UUID playerId = player.getUniqueId();

        if (!(event.getInventory().getHolder() instanceof AuctionGUIHolder holder)) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        Long lastClick = clickCooldownMap.get(playerId);
        if (lastClick != null && (currentTime - lastClick) < CLICK_COOLDOWN_MS) {
            event.setCancelled(true);
            return;
        }
        clickCooldownMap.put(playerId, currentTime);

        GUIType guiType = holder.getGuiType();
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (container.has(guiItemKey, PersistentDataType.BYTE)) {
            String itemType = container.get(guiItemTypeKey, PersistentDataType.STRING);
            if (itemType != null) {
                try {
                    handleSpecialItemClick(player, holder, GuiItemType.valueOf(itemType));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().log(Level.WARNING, "Invalid GUI item type: " + itemType);
                }
                return;
            }
        }

        int slot = event.getRawSlot();
        switch (guiType) {
            case REMOVE_CONFIRM -> handleConfirmRemovalClick(player, slot);
            case PLAYER_ITEMS -> handlePlayerItemClick(player, slot);
            case AUCTION_HOUSE -> handleAuctionItemClick(player, slot);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        UUID playerId = player.getUniqueId();

        if (event.getInventory().getHolder() instanceof AuctionGUIHolder holder) {
            if (holder.getGuiType() == GUIType.PLAYER_ITEMS &&
                    pendingRemovalMap.containsKey(playerId)) {
                return;
            }

            cleanupPlayerState(playerId);
        }
    }
    private void cleanupPlayerState(UUID playerId) {
        auctionViewingMap.remove(playerId);

        if (!isInGUITransition(playerId)) {
            playerItemsViewingMap.remove(playerId);
            pendingRemovalMap.remove(playerId);
        }

        if (!isInGUITransition(playerId)) {
            playerGUITypeMap.remove(playerId);
        }

        playerPageMap.remove(playerId);
        clickCooldownMap.remove(playerId);
    }

    private void handleSpecialItemClick(Player player, AuctionGUIHolder holder, GuiItemType itemType) {
        UUID playerId = player.getUniqueId();
        int currentPage = holder.getPage();

        switch (itemType) {
            case PREVIOUS_PAGE -> {
                if (currentPage > 1) {
                    openGUIForType(player, holder.getGuiType(), currentPage - 1);
                }
            }
            case NEXT_PAGE -> openGUIForType(player, holder.getGuiType(), currentPage + 1);
            case REFRESH -> {
                openGUIForType(player, holder.getGuiType(), currentPage);
                sendTranslatedMessage(player, "commands.auction.gui.refresh", "<green>Auction refreshed!");
            }
            case MY_ITEMS -> openGUIForType(player, GUIType.PLAYER_ITEMS, 1);
            case CLOSE -> player.closeInventory();
            case BALANCE_INFO -> {
                double balance = economyManager.getBalance(player);
                sendTranslatedMessage(player, "commands.auction.gui.balance.message",
                        "<green>Balance: <yellow>${balance}",
                        ComponentPlaceholder.of("{balance}", economyManager.format(balance)));
            }
            case CONFIRM_REMOVE -> handleConfirmRemoval(player, true);
            case CANCEL_REMOVE -> handleConfirmRemoval(player, false);
            default -> {}
        }
    }

    private void handleConfirmRemovalClick(Player player, int slot) {
        boolean confirmed = (slot == 11);
        handleConfirmRemoval(player, confirmed);
    }

    private void handleConfirmRemoval(Player player, boolean confirmed) {
        UUID playerId = player.getUniqueId();
        UUID itemId = pendingRemovalMap.remove(playerId);

        if (itemId == null) {
            plugin.getLogger().log(Level.WARNING, "[Auction] No pending removal for " + player.getName());
            sendTranslatedMessage(player, "commands.auction.remove.no-pending",
                    "<red>No pending removal found!");
            player.closeInventory();
            return;
        }

        if (!confirmed) {
            sendTranslatedMessage(player, "commands.auction.remove.cancelled",
                    "<gray>Removal cancelled.");
            openPlayerItemsGUI(player, getCurrentPage(playerId, GUIType.PLAYER_ITEMS));
            return;
        }

        storage.getItemData(itemId).thenAccept(optItem -> {
            if (optItem.isEmpty()) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    sendTranslatedMessage(player, "commands.auction.remove.not-found",
                            "<red>Item not found!");
                    openPlayerItemsGUI(player, 1);
                });
                return;
            }

            AuctionItem item = optItem.get();
            storage.removeItem(itemId).thenRun(() -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(item.getItem().clone());

                    if (!remaining.isEmpty()) {
                        remaining.values().forEach(drop ->
                                player.getWorld().dropItemNaturally(player.getLocation(), drop)
                        );
                        sendTranslatedMessage(player, "commands.auction.remove.inventory-full",
                                "<yellow>Item dropped on the ground (inventory full)");
                    }

                    sendTranslatedMessage(player, "commands.auction.remove.success",
                            "<green>Item removed successfully!");
                    openPlayerItemsGUI(player, getCurrentPage(playerId, GUIType.PLAYER_ITEMS));
                });
            }).exceptionally(ex -> {
                plugin.getLogger().log(Level.SEVERE, "[Auction] Failed to remove item", ex);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    sendTranslatedMessage(player, "commands.auction.remove.failed",
                            "<red>Failed to remove item. Please try again.");
                    openPlayerItemsGUI(player, 1);
                });
                return null;
            });
        });
    }

    private void handlePlayerItemClick(Player player, int slot) {
        UUID playerId = player.getUniqueId();
        Map<Integer, UUID> slotMap = playerItemsViewingMap.get(playerId);

        if (slotMap == null) {
            plugin.getLogger().log(Level.WARNING, "[Auction] No slot map for " + player.getName() + ". This may indicate a state sync issue.");
            openPlayerItemsGUI(player, 1);
            return;
        }

        UUID itemId = slotMap.get(slot);
        if (itemId == null) return;

        pendingRemovalMap.put(playerId, itemId);
        openRemoveConfirmGUI(player);
    }

    private void handleAuctionItemClick(Player player, int slot) {
        UUID playerId = player.getUniqueId();
        Map<Integer, UUID> slotMap = auctionViewingMap.get(playerId);
        if (slotMap == null) {
            plugin.getLogger().log(Level.WARNING, "[Auction] No auction slot map for " + player.getName());
            return;
        }

        UUID itemId = slotMap.get(slot);
        if (itemId == null) return;

        storage.getItemData(itemId).thenAccept(optItem -> {
            if (optItem.isEmpty()) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    sendTranslatedMessage(player, "commands.auction.purchase.not-found",
                            "<red>Item no longer available!");
                });
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                processPurchase(player, optItem.get());
            });
        }).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, "[Auction] Failed to get item data", ex);
            return null;
        });
    }

    private void processPurchase(Player player, AuctionItem item) {
        if (!economyManager.isEnabled()) {
            sendTranslatedMessage(player, "commands.auction.purchase.no-economy",
                    "<red>✗ Economy system is not available.");
            return;
        }

        double balance = economyManager.getBalance(player);
        if (balance < item.getPrice()) {
            sendTranslatedMessage(player, "commands.auction.purchase.not-enough-money",
                    "<red>You don't have enough money!");
            return;
        }

        CompletableFuture.runAsync(() -> {
            EconomyResponse withdrawResponse = economyManager.withdraw(player, item.getPrice());

            if (!withdrawResponse.success()) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    sendTranslatedMessage(player, "commands.auction.purchase.failed",
                            "<red>Transaction failed: " + withdrawResponse.errorMessage);
                });
                return;
            }

            Player sellerPlayer = Bukkit.getPlayer(item.getSeller());
            if (sellerPlayer != null && sellerPlayer.isOnline()) {
                EconomyResponse depositResponse = economyManager.deposit(sellerPlayer, item.getPrice());
                if (!depositResponse.success()) {
                    economyManager.deposit(player, item.getPrice());
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        sendTranslatedMessage(player, "commands.auction.purchase.failed",
                                "<red>Transaction failed: Could not pay seller");
                    });
                    return;
                }

                sendTranslatedMessage(sellerPlayer, "commands.auction.purchase.seller-message",
                        "<green>{buyer} purchased your item for ${price}!",
                        ComponentPlaceholder.of("{buyer}", player.getName()),
                        ComponentPlaceholder.of("{price}", economyManager.format(item.getPrice())));
            }

            storage.removeItem(item.getId()).thenAccept(v -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.getInventory().addItem(item.getItem().clone());

                    sendTranslatedMessage(player, "commands.auction.purchase.success",
                            "<green>Purchased for <yellow>${price}</yellow>!",
                            ComponentPlaceholder.of("{price}", economyManager.format(item.getPrice())));

                    UUID playerId = player.getUniqueId();
                    int currentPage = getCurrentPage(playerId, GUIType.AUCTION_HOUSE);
                    openAuctionGUI(player, currentPage);
                });
            }).exceptionally(ex -> {
                plugin.getLogger().log(Level.SEVERE, "[Auction] Failed to remove item after purchase", ex);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    sendTranslatedMessage(player, "commands.auction.purchase.failed",
                            "<red>Failed to complete purchase. Please try again.");
                });
                return null;
            });
        });
    }

    private void openGUIForType(Player player, GUIType guiType, int page) {
        if (!economyManager.isEnabled()) {
            sendTranslatedMessage(player, "commands.auction.no-economy",
                    "<red>✗ Economy system is not available.");
            return;
        }

        switch (guiType) {
            case AUCTION_HOUSE -> openAuctionGUI(player, page);
            case PLAYER_ITEMS -> openPlayerItemsGUI(player, page);
            case REMOVE_CONFIRM -> {
                plugin.getLogger().log(Level.WARNING, "Cannot directly open REMOVE_CONFIRM GUI. Use openRemoveConfirmGUI() instead.");
                openPlayerItemsGUI(player, 1);
            }
        }
    }

    private void setCurrentPage(UUID playerId, int page) {
        playerPageMap.put(playerId, page);
    }

    private int getCurrentPage(UUID playerId, GUIType guiType) {
        return playerPageMap.getOrDefault(playerId, 1);
    }

    public void openAuctionGUI(Player player, int page) {
        if (!economyManager.isEnabled()) {
            sendTranslatedMessage(player, "commands.auction.no-economy",
                    "<red>✗ Economy system is not available.");
            return;
        }

        UUID playerId = player.getUniqueId();

        storage.getAllActiveItems().thenAccept(items -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    openAuctionGUISync(player, page, items);
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "[Auction] Failed to open auction GUI", e);
                    sendTranslatedMessage(player, "general.error", "<red>An error occurred. Please try again.");
                }
            });
        }).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, "[Auction] Failed to load auction items", ex);
            return null;
        });
    }

    private void openAuctionGUISync(Player player, int page, List<AuctionItem> items) {
        UUID playerId = player.getUniqueId();

        items = filterExpired(items);
        int totalPages = Math.max(1, (int) Math.ceil((double) items.size() / config.itemsPerPage));
        page = Math.min(page, totalPages);
        setCurrentPage(playerId, page);

        Component title = getTranslatedComponent(player, "commands.auction.gui.titles.auction",
                "<green>Auction House - Page {page}",
                ComponentPlaceholder.of("{page}", String.valueOf(page)));

        Inventory inv = Bukkit.createInventory(
                new AuctionGUIHolder(GUIType.AUCTION_HOUSE, page),
                config.guiRows * 9,
                title
        );

        addBorder(inv);

        Map<Integer, UUID> slotMap = new HashMap<>();
        int start = (page - 1) * config.itemsPerPage;
        int end = Math.min(start + config.itemsPerPage, items.size());

        for (int i = start; i < end; i++) {
            AuctionItem item = items.get(i);
            int slot = getSlot(i - start);
            slotMap.put(slot, item.getId());
            inv.setItem(slot, createDisplayItem(player, item, false));
        }

        auctionViewingMap.put(playerId, slotMap);
        playerGUITypeMap.put(playerId, GUIType.AUCTION_HOUSE);

        inv.setItem(45, createNavigationItem(player, Material.ARROW, GuiItemType.PREVIOUS_PAGE,
                "commands.auction.gui.navigation.prev", "<yellow>← Previous Page"));
        inv.setItem(0, createNavigationItem(player, Material.PAPER, GuiItemType.REFRESH,
                "commands.auction.gui.navigation.refresh", "<aqua>↻ Refresh"));
        inv.setItem(4, createPlayerBalanceItem(player));
        inv.setItem(8, createNavigationItem(player, Material.CHEST, GuiItemType.MY_ITEMS,
                "commands.auction.gui.navigation.my-items", "<gold>☰ My Items"));
        inv.setItem(49, createNavigationItem(player, Material.BARRIER, GuiItemType.CLOSE,
                "commands.auction.gui.navigation.close", "<red>✕ Close"));
        inv.setItem(53, createNavigationItem(player, Material.ARROW, GuiItemType.NEXT_PAGE,
                "commands.auction.gui.navigation.next", "<yellow>Next Page →"));

        player.openInventory(inv);
    }

    public void openPlayerItemsGUI(Player player, int page) {
        if (!economyManager.isEnabled()) {
            sendTranslatedMessage(player, "commands.auction.no-economy",
                    "<red>✗ Economy system is not available.");
            return;
        }

        UUID playerId = player.getUniqueId();

        storage.getPlayerItems(playerId).thenAccept(items -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    openPlayerItemsGUISync(player, page, items);
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "[Auction] Failed to open player items GUI", e);
                    sendTranslatedMessage(player, "general.error", "<red>An error occurred. Please try again.");
                }
            });
        }).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, "[Auction] Failed to load player items", ex);
            return null;
        });
    }

    private void openPlayerItemsGUISync(Player player, int page, List<AuctionItem> items) {
        UUID playerId = player.getUniqueId();

        items = filterExpired(items);
        int totalPages = Math.max(1, (int) Math.ceil((double) items.size() / config.itemsPerPage));
        page = Math.min(page, totalPages);
        setCurrentPage(playerId, page);

        Component title = getTranslatedComponent(player, "commands.auction.gui.titles.my-items",
                "<green>Your Items - Page {page}",
                ComponentPlaceholder.of("{page}", String.valueOf(page)));

        Inventory inv = Bukkit.createInventory(
                new AuctionGUIHolder(GUIType.PLAYER_ITEMS, page),
                config.guiRows * 9,
                title
        );

        addBorder(inv);

        Map<Integer, UUID> slotMap = new HashMap<>();
        int start = (page - 1) * config.itemsPerPage;
        int end = Math.min(start + config.itemsPerPage, items.size());

        for (int i = start; i < end; i++) {
            AuctionItem item = items.get(i);
            int slot = getSlot(i - start);
            slotMap.put(slot, item.getId());
            inv.setItem(slot, createDisplayItem(player, item, true));
        }

        playerItemsViewingMap.put(playerId, slotMap);
        playerGUITypeMap.put(playerId, GUIType.PLAYER_ITEMS);

        inv.setItem(45, createNavigationItem(player, Material.ARROW, GuiItemType.PREVIOUS_PAGE,
                "commands.auction.gui.navigation.prev", "<yellow>← Previous Page"));
        inv.setItem(53, createNavigationItem(player, Material.ARROW, GuiItemType.NEXT_PAGE,
                "commands.auction.gui.navigation.next", "<yellow>Next Page →"));
        inv.setItem(49, createNavigationItem(player, Material.BARRIER, GuiItemType.CLOSE,
                "commands.auction.gui.navigation.close", "<red>✕ Close"));

        player.openInventory(inv);
    }

    private void openRemoveConfirmGUI(Player player) {
        Component title = getTranslatedComponent(player, "commands.auction.gui.titles.remove-confirm",
                "<red>Confirm Removal");

        Inventory inv = Bukkit.createInventory(
                new AuctionGUIHolder(GUIType.REMOVE_CONFIRM, 1),
                27,
                title
        );

        inv.setItem(11, createNavigationItem(player, Material.GREEN_WOOL, GuiItemType.CONFIRM_REMOVE,
                "commands.auction.gui.remove.confirm", "<green>✓ Confirm Removal"));
        inv.setItem(15, createNavigationItem(player, Material.RED_WOOL, GuiItemType.CANCEL_REMOVE,
                "commands.auction.gui.remove.cancel", "<red>✗ Cancel"));

        playerGUITypeMap.put(player.getUniqueId(), GUIType.REMOVE_CONFIRM);
        player.openInventory(inv);
    }

    private ItemStack createNavigationItem(Player player, Material material, GuiItemType itemType,
                                           String messageKey, String defaultValue) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        Component nameComponent = getTranslatedComponent(player, messageKey, defaultValue);
        meta.displayName(nameComponent);

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(guiItemKey, PersistentDataType.BYTE, (byte) 1);
        container.set(guiItemTypeKey, PersistentDataType.STRING, itemType.name());

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createDisplayItem(Player player, AuctionItem item, boolean forSeller) {
        ItemStack display = item.getItem().clone();
        ItemMeta meta = display.getItemMeta();
        if (meta == null) return display;

        Component nameComponent = getTranslatedComponent(player, "commands.auction.gui.item.name",
                "<green>Price: <yellow>${price}",
                ComponentPlaceholder.of("{price}", economyManager.format(item.getPrice())));
        meta.displayName(nameComponent);

        List<Component> lore = new ArrayList<>();

        if (forSeller) {
            lore.add(getTranslatedComponent(player, "commands.auction.gui.item.lore.seller-price",
                    "<gray>Your price: <yellow>${price}",
                    ComponentPlaceholder.of("{price}", economyManager.format(item.getPrice()))));
        } else {
            lore.add(getTranslatedComponent(player, "commands.auction.gui.item.lore.seller",
                    "<gray>Seller: <yellow>{seller}",
                    ComponentPlaceholder.of("{seller}", getSellerName(item.getSeller()))));
        }

        lore.add(getTranslatedComponent(player, "commands.auction.gui.item.lore.expires",
                "<gray>Expires: <yellow>{date}",
                ComponentPlaceholder.of("{date}", formatDate(item.getExpiration()))));

        String clickMessageKey = forSeller ? "commands.auction.gui.item.lore.seller-click" : "commands.auction.gui.item.lore.buyer-click";
        String clickDefault = forSeller ? "<yellow>Click to remove" : "<yellow>Click to purchase";
        lore.add(getTranslatedComponent(player, clickMessageKey, clickDefault));

        meta.lore(lore);
        display.setItemMeta(meta);
        return display;
    }

    private ItemStack createPlayerBalanceItem(Player player) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta == null) return skull;

        double balance = economyManager.getBalance(player);

        Component nameComponent = getTranslatedComponent(player, "commands.auction.gui.balance.item-name",
                "<green>Balance: <yellow>${balance}",
                ComponentPlaceholder.of("{balance}", economyManager.format(balance)));
        meta.displayName(nameComponent);

        meta.setOwningPlayer(player);
        skull.setItemMeta(meta);
        return skull;
    }

    private void addBorder(Inventory inv) {
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        if (meta == null) return;

        meta.displayName(Component.empty());

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(guiItemKey, PersistentDataType.BYTE, (byte) 1);
        container.set(guiItemTypeKey, PersistentDataType.STRING, GuiItemType.BORDER.name());

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
        try {
            return LocalDate.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
                    .format(DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to format date: " + timestamp, e);
            return "Unknown";
        }
    }

    private List<AuctionItem> filterExpired(List<AuctionItem> items) {
        long now = System.currentTimeMillis();
        return items.stream().filter(item -> item.getExpiration() > now).toList();
    }

    private void sendTranslatedMessage(Player player, String key, String defaultValue, ComponentPlaceholder... placeholders) {
        try {
            Component message = langManager.getMessageFor(player, key, defaultValue, placeholders);
            if (message == null) {
                plugin.getLogger().log(Level.WARNING, "Translation resulted in null for key: " + key);
                message = miniMessage.deserialize(defaultValue);
            }
            player.sendMessage(message);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to send translated message for key: " + key, e);
            player.sendMessage(miniMessage.deserialize(defaultValue));
        }
    }

    private Component getTranslatedComponent(Player player, String key, String defaultValue, ComponentPlaceholder... placeholders) {
        try {
            Component component = langManager.getMessageFor(player, key, defaultValue, placeholders);
            if (component == null) {
                plugin.getLogger().log(Level.WARNING, "Component translation resulted in null for key: " + key);
                return miniMessage.deserialize(defaultValue);
            }
            return component;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to translate component for key: " + key, e);
            return miniMessage.deserialize(defaultValue);
        }
    }

    private boolean isInGUITransition(UUID playerId) {
        return pendingRemovalMap.containsKey(playerId);
    }
}