package net.lunark.io.economy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.lunark.io.language.LanguageManager;
import net.lunark.io.language.PlayerLanguageManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ShopGUIManager {
    private static final MiniMessage mini = MiniMessage.miniMessage();

    private final Plugin plugin;
    private final PlayerLanguageManager langManager;
    private final ShopStorage storage;
    private final ShopConfig config;
    private final Economy economy;

    // Runtime state (still needed for open inventories)
    private final Map<UUID, String> openSection = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> currentPage = new ConcurrentHashMap<>();

    public ShopGUIManager(Plugin plugin, PlayerLanguageManager langManager,
                          ShopStorage storage, ShopConfig config, Economy economy) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.storage = storage;
        this.config = config;
        this.economy = economy;
    }

    public void openMainGUI(Player player) {
        MainShopConfig main = ShopConfigLoader.loadMainConfig(new File(config.getShopFolder(), "main.yml"));
        Component title = langManager.getMessageFor(player, "economy.shop.main-title", "<green>Main Shop");

        Inventory inv = Bukkit.createInventory(null, config.mainSize, title);

        // Load layout items
        main.layout.forEach((slot, item) -> {
            ItemStack stack = createItem(item.material, item.name, null, 1);
            inv.setItem(slot, stack);
        });

        // Load section buttons
        main.sectionButtons.forEach((slot, button) -> {
            ItemStack stack = createItem(button.material, button.name, button.lore, 1);
            inv.setItem(slot, stack);
        });

        // Close button
        ItemStack close = createItem(Material.BARRIER, "<red>Close", null, 1);
        inv.setItem(config.closeButtonSlot, close);

        player.openInventory(inv);
        openSection.remove(player.getUniqueId());
    }

    public void openSectionGUI(Player player, String fileName) {
        openSectionGUI(player, fileName, 1);
    }

    public void openSectionGUI(Player player, String fileName, int page) {
        ShopSectionConfig section = ShopConfigLoader.loadSectionConfig(new File(config.getShopFolder(), fileName));
        if (section == null) {
            player.sendMessage(langManager.getMessageFor(player, "economy.shop.no-section",
                    "<red>Shop section not found.",
                    LanguageManager.ComponentPlaceholder.of("{section}", fileName)));
            return;
        }

        // Use section title or generate from filename
        String displayTitle = section.title != null ? section.title :
                fileName.replace(".yml", "").replace("-", " ");
        Component title = langManager.getMessageFor(player, "economy.shop.section-title",
                "<green>{section} Shop",
                LanguageManager.ComponentPlaceholder.of("{section}", displayTitle));

        Inventory inv = Bukkit.createInventory(null, section.size, title);

        // Layout items
        section.layout.forEach((slot, item) -> {
            ItemStack stack = createItem(item.material, item.name, item.lore, 1);
            inv.setItem(slot, stack);
        });

        // Shop items for current page
        section.items.values().stream()
                .filter(item -> item.page == page)
                .forEach(item -> {
                    ItemStack stack = createItem(item.material, item.name, item.lore, item.amount);
                    inv.setItem(item.slot, stack);
                });

        // Player head with balance
        int headSlot = section.playerHeadSlot >= 0 ? section.playerHeadSlot : config.playerHeadSlot;
        if (headSlot >= 0) {
            ItemStack skull = createPlayerHead(player);
            inv.setItem(headSlot, skull);
        }

        // Close/Back button
        int closeSlot = section.closeButtonSlot >= 0 ? section.closeButtonSlot : config.closeButtonSlot;
        if (closeSlot >= 0) {
            ItemStack back = createItem(Material.BARRIER, "<red>Back", null, 1);
            inv.setItem(closeSlot, back);
        }

        // Navigation
        if (page > 1) {
            ItemStack prev = createItem(Material.ARROW, "<gray>Previous Page", null, 1);
            inv.setItem(45, prev);
        }
        if (page < section.pages) {
            ItemStack next = createItem(Material.ARROW, "<gray>Next Page", null, 1);
            inv.setItem(53, next);
        }

        player.openInventory(inv);
        openSection.put(player.getUniqueId(), fileName);
        currentPage.put(player.getUniqueId(), page);

        // Persist state
        storage.setPlayerSection(player.getUniqueId(), fileName);
        storage.setPlayerPage(player.getUniqueId(), page);
    }

    public void handleClick(Player player, int slot, String clickType, Inventory inv) {
        UUID uuid = player.getUniqueId();
        String sectionFile = openSection.get(uuid);
        int page = currentPage.getOrDefault(uuid, 1);

        // Determine if main menu or section
        boolean isMain = sectionFile == null;

        // CRITICAL FIX: Add a cooldown map to prevent rapid clicks
        if (clickCooldowns.getOrDefault(uuid, 0L) > System.currentTimeMillis()) {
            return; // Ignore clicks within 200ms
        }
        clickCooldowns.put(uuid, System.currentTimeMillis() + 200);

        if (isMain) {
            handleMainMenuClick(player, slot);
        } else {
            handleSectionClick(player, slot, page, sectionFile, clickType);
        }
    }

    // Add this field to ShopGUIManager
    private final Map<UUID, Long> clickCooldowns = new ConcurrentHashMap<>();

    private void handleMainMenuClick(Player player, int slot) {
        MainShopConfig main = ShopConfigLoader.loadMainConfig(new File(config.getShopFolder(), "main.yml"));

        if (slot == config.closeButtonSlot) {
            player.closeInventory();
            return;
        }

        MainShopConfig.SectionButton button = main.sectionButtons.get(slot);
        if (button != null) {
            // FIX: Schedule to next tick to prevent click propagation
            Bukkit.getScheduler().runTask(plugin, () -> {
                openSectionGUI(player, button.file, 1);
            });
        }
    }

    private void handleSectionClick(Player player, int slot, int page, String sectionFile, String clickType) {
        ShopSectionConfig section = ShopConfigLoader.loadSectionConfig(new File(config.getShopFolder(), sectionFile));

        // Navigation
        if (slot == 45 && page > 1) {
            Bukkit.getScheduler().runTask(plugin, () -> openSectionGUI(player, sectionFile, page - 1));
            return;
        }
        if (slot == 53 && page < section.pages) {
            Bukkit.getScheduler().runTask(plugin, () -> openSectionGUI(player, sectionFile, page + 1));
            return;
        }

        // Close button
        int closeSlot = section.closeButtonSlot >= 0 ? section.closeButtonSlot : config.closeButtonSlot;
        if (slot == closeSlot) {
            Bukkit.getScheduler().runTask(plugin, () -> openMainGUI(player));
            return;
        }

        // Shop item interaction (keep this synchronous to ensure item processing is immediate)
        for (ShopSectionConfig.ShopItem item : section.items.values()) {
            if (item.page == page && item.slot == slot && item.clickable) {
                boolean isLeftClick = clickType.equals("LEFT");
                boolean isRightClick = clickType.equals("RIGHT");

                if (isLeftClick && item.buyPrice > 0) {
                    handleBuy(player, item);
                } else if (isRightClick && item.sellPrice > 0 && config.enableSell) {
                    handleSell(player, item);
                }
                break;
            }
        }
    }

    private void handleBuy(Player player, ShopSectionConfig.ShopItem item) {
        double balance = economy.getBalance(player);
        if (balance >= item.buyPrice) {
            economy.withdrawPlayer(player, item.buyPrice);
            player.getInventory().addItem(new ItemStack(item.material, item.amount));

            player.sendMessage(langManager.getMessageFor(player, "economy.shop.buy-success",
                    "<green>You bought {amount}x {item} for {symbol}{price}",
                    LanguageManager.ComponentPlaceholder.of("{amount}", item.amount),
                    LanguageManager.ComponentPlaceholder.of("{item}", item.name),
                    LanguageManager.ComponentPlaceholder.of("{price}", String.format("%.2f", item.buyPrice)),
                    LanguageManager.ComponentPlaceholder.of("{symbol}", config.currencySymbol)));

            // Refresh GUI
            String section = openSection.get(player.getUniqueId());
            int page = currentPage.getOrDefault(player.getUniqueId(), 1);
            if (section != null) {
                openSectionGUI(player, section, page);
            }
        } else {
            player.sendMessage(langManager.getMessageFor(player, "economy.shop.cannot-afford",
                    "<red>You cannot afford {item} (cost: {symbol}{price})",
                    LanguageManager.ComponentPlaceholder.of("{item}", item.name),
                    LanguageManager.ComponentPlaceholder.of("{price}", String.format("%.2f", item.buyPrice)),
                    LanguageManager.ComponentPlaceholder.of("{symbol}", config.currencySymbol)));
        }
    }

    private void handleSell(Player player, ShopSectionConfig.ShopItem item) {
        if (!player.getInventory().containsAtLeast(new ItemStack(item.material), item.amount)) {
            player.sendMessage(langManager.getMessageFor(player, "economy.shop.no-items",
                    "<red>You don't have enough {item} to sell",
                    LanguageManager.ComponentPlaceholder.of("{item}", item.name)));
            return;
        }

        removeItems(player, item.material, item.amount);
        economy.depositPlayer(player, item.sellPrice);

        player.sendMessage(langManager.getMessageFor(player, "economy.shop.sell-success",
                "<green>You sold {amount}x {item} for {symbol}{price}",
                LanguageManager.ComponentPlaceholder.of("{amount}", item.amount),
                LanguageManager.ComponentPlaceholder.of("{item}", item.name),
                LanguageManager.ComponentPlaceholder.of("{price}", String.format("%.2f", item.sellPrice)),
                LanguageManager.ComponentPlaceholder.of("{symbol}", config.currencySymbol)));

        // Refresh GUI
        String section = openSection.get(player.getUniqueId());
        int page = currentPage.getOrDefault(player.getUniqueId(), 1);
        if (section != null) {
            openSectionGUI(player, section, page);
        }
    }

    public void refreshOpenInventories() {
        new HashSet<>(openSection.keySet()).forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.getOpenInventory().getTopInventory().getHolder() == null) {
                String section = openSection.get(uuid);
                int page = currentPage.getOrDefault(uuid, 1);
                if (section == null) {
                    openMainGUI(player);
                } else {
                    openSectionGUI(player, section, page);
                }
            }
        });
    }

    private void removeItems(Player player, Material material, int amount) {
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack stack = contents[i];
            if (stack != null && stack.getType() == material) {
                int stackAmount = stack.getAmount();
                if (stackAmount <= remaining) {
                    player.getInventory().clear(i);
                    remaining -= stackAmount;
                } else {
                    stack.setAmount(stackAmount - remaining);
                    remaining = 0;
                }
                if (remaining <= 0) break;
            }
        }
        player.updateInventory();
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        return createItem(material, name, lore, 1);
    }

    private ItemStack createItem(Material material, String name, List<String> lore, int amount) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (name != null) {
                meta.displayName(mini.deserialize(name));
            }
            if (lore != null) {
                List<Component> loreComponents = lore.stream()
                        .map(line -> mini.deserialize(line))
                        .toList();
                meta.lore(loreComponents);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createPlayerHead(Player player) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(player);
            meta.displayName(langManager.getMessageFor(player, "economy.shop.balance-display",
                    "<green>Your Balance: <gold>{balance}",
                    LanguageManager.ComponentPlaceholder.of("{balance}",
                            String.format("%.2f", economy.getBalance(player)))));
            skull.setItemMeta(meta);
        }
        return skull;
    }
}