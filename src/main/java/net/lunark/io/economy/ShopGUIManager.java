package net.lunark.io.economy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.lunark.io.commands.config.ShopConfig;
import net.lunark.io.language.PlayerLanguageManager;
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
import java.util.concurrent.ConcurrentHashMap;

public class ShopGUIManager {
    private static final MiniMessage mini = MiniMessage.miniMessage();
    private static final long CLICK_COOLDOWN_MS = 200;

    private final Plugin plugin;
    private final PlayerLanguageManager langManager;
    private final ShopStorage storage;
    private final ShopConfig config;
    private final ServerEssentialsEconomy economy;

    private final Map<UUID, String> openSection = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> currentPage = new ConcurrentHashMap<>();
    private final Map<UUID, Long> clickCooldowns = new ConcurrentHashMap<>();

    public ShopGUIManager(Plugin plugin, PlayerLanguageManager langManager,
                          ShopStorage storage, ShopConfig config, ServerEssentialsEconomy economy) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.storage = storage;
        this.config = config;
        this.economy = economy;
    }

    public void openMainGUI(Player player) {
        File mainFile = new File(config.getShopFolder(), "main.yml");
        if (!mainFile.exists()) {
            player.sendMessage(langManager.getMessageFor(player, "economy.shop.no-main-config",
                    "<red>Shop configuration not found. Please contact an administrator."));
            return;
        }

        MainShopConfig main = ShopConfigLoader.loadMainConfig(mainFile);
        Component title = langManager.getMessageFor(player, "economy.shop.main-title",
                "<green>Main Shop");

        Inventory inv = Bukkit.createInventory(null, config.mainSize, title);

        main.layout.forEach((slot, item) -> {
            ItemStack stack = createItem(item.material, item.name, null, 1);
            inv.setItem(slot, stack);
        });

        main.sectionButtons.forEach((slot, button) -> {
            ItemStack stack = createItem(button.material, button.name, button.lore, 1);
            inv.setItem(slot, stack);
        });

        ItemStack close = createItem(Material.BARRIER, "<red>Close", null, 1);
        inv.setItem(config.closeButtonSlot, close);

        player.openInventory(inv);
        openSection.remove(player.getUniqueId());
    }

    public void openSectionGUI(Player player, String fileName) {
        openSectionGUI(player, fileName, 1);
    }

    public void openSectionGUI(Player player, String fileName, int page) {
        File sectionFile = new File(config.getShopFolder(), fileName);
        if (!sectionFile.exists()) {
            player.sendMessage(langManager.getMessageFor(player, "economy.shop.no-section",
                    "<red>Shop section not found: <white>{section}",
                    net.lunark.io.language.LanguageManager.ComponentPlaceholder.of("{section}", fileName)));
            return;
        }

        ShopSectionConfig section = ShopConfigLoader.loadSectionConfig(sectionFile);
        String displayTitle = section.title != null ? section.title :
                fileName.replace(".yml", "").replace("-", " ");
        Component title = langManager.getMessageFor(player, "economy.shop.section-title",
                "<green>{section} Shop",
                net.lunark.io.language.LanguageManager.ComponentPlaceholder.of("{section}", displayTitle));

        Inventory inv = Bukkit.createInventory(null, section.size, title);

        section.layout.forEach((slot, item) -> {
            ItemStack stack = createItem(item.material, item.name, item.lore, 1);
            inv.setItem(slot, stack);
        });

        section.items.values().stream()
                .filter(item -> item.page == page)
                .forEach(item -> {
                    ItemStack stack = createItem(item.material, item.name, item.lore, item.amount);
                    inv.setItem(item.slot, stack);
                });

        int headSlot = section.playerHeadSlot >= 0 ? section.playerHeadSlot : config.playerHeadSlot;
        if (headSlot >= 0) {
            ItemStack skull = createPlayerHead(player);
            inv.setItem(headSlot, skull);
        }

        int closeSlot = section.closeButtonSlot >= 0 ? section.closeButtonSlot : config.closeButtonSlot;
        if (closeSlot >= 0) {
            ItemStack back = createItem(Material.BARRIER, "<red>Back", null, 1);
            inv.setItem(closeSlot, back);
        }

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

        storage.setPlayerSection(player.getUniqueId(), fileName);
        storage.setPlayerPage(player.getUniqueId(), page);
    }

    public void handleClick(Player player, int slot, String clickType, Inventory inv) {
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        if (clickCooldowns.getOrDefault(uuid, 0L) > currentTime) {
            return;
        }
        clickCooldowns.put(uuid, currentTime + CLICK_COOLDOWN_MS);

        String sectionFile = openSection.get(uuid);
        boolean isMain = sectionFile == null;

        if (isMain) {
            handleMainMenuClick(player, slot);
        } else {
            int page = currentPage.getOrDefault(uuid, 1);
            handleSectionClick(player, slot, page, sectionFile, clickType);
        }
    }

    private void handleMainMenuClick(Player player, int slot) {
        File mainFile = new File(config.getShopFolder(), "main.yml");
        if (!mainFile.exists()) return;

        MainShopConfig main = ShopConfigLoader.loadMainConfig(mainFile);

        if (slot == config.closeButtonSlot) {
            player.closeInventory();
            return;
        }

        MainShopConfig.SectionButton button = main.sectionButtons.get(slot);
        if (button != null) {
            Bukkit.getScheduler().runTask(plugin, () -> openSectionGUI(player, button.file, 1));
        }
    }

    private void handleSectionClick(Player player, int slot, int page, String sectionFile, String clickType) {
        File sectionFileObject = new File(config.getShopFolder(), sectionFile);
        if (!sectionFileObject.exists()) return;

        ShopSectionConfig section = ShopConfigLoader.loadSectionConfig(sectionFileObject);

        if (slot == 45 && page > 1) {
            Bukkit.getScheduler().runTask(plugin, () -> openSectionGUI(player, sectionFile, page - 1));
            return;
        }
        if (slot == 53 && page < section.pages) {
            Bukkit.getScheduler().runTask(plugin, () -> openSectionGUI(player, sectionFile, page + 1));
            return;
        }

        int closeSlot = section.closeButtonSlot >= 0 ? section.closeButtonSlot : config.closeButtonSlot;
        if (slot == closeSlot) {
            Bukkit.getScheduler().runTask(plugin, () -> openMainGUI(player));
            return;
        }

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
                    net.lunark.io.language.LanguageManager.ComponentPlaceholder.of("{amount}", item.amount),
                    net.lunark.io.language.LanguageManager.ComponentPlaceholder.of("{item}", item.name),
                    net.lunark.io.language.LanguageManager.ComponentPlaceholder.of("{price}", String.format("%.2f", item.buyPrice)),
                    net.lunark.io.language.LanguageManager.ComponentPlaceholder.of("{symbol}", config.currencySymbol)));

            refreshGUI(player);
        } else {
            player.sendMessage(langManager.getMessageFor(player, "economy.shop.cannot-afford",
                    "<red>You cannot afford {item} (cost: {symbol}{price})",
                    net.lunark.io.language.LanguageManager.ComponentPlaceholder.of("{item}", item.name),
                    net.lunark.io.language.LanguageManager.ComponentPlaceholder.of("{price}", String.format("%.2f", item.buyPrice)),
                    net.lunark.io.language.LanguageManager.ComponentPlaceholder.of("{symbol}", config.currencySymbol)));
        }
    }

    private void handleSell(Player player, ShopSectionConfig.ShopItem item) {
        if (!player.getInventory().containsAtLeast(new ItemStack(item.material), item.amount)) {
            player.sendMessage(langManager.getMessageFor(player, "economy.shop.no-items",
                    "<red>You don't have enough {item} to sell",
                    net.lunark.io.language.LanguageManager.ComponentPlaceholder.of("{item}", item.name)));
            return;
        }

        removeItems(player, item.material, item.amount);
        economy.depositPlayer(player, item.sellPrice);

        player.sendMessage(langManager.getMessageFor(player, "economy.shop.sell-success",
                "<green>You sold {amount}x {item} for {symbol}{price}",
                net.lunark.io.language.LanguageManager.ComponentPlaceholder.of("{amount}", item.amount),
                net.lunark.io.language.LanguageManager.ComponentPlaceholder.of("{item}", item.name),
                net.lunark.io.language.LanguageManager.ComponentPlaceholder.of("{price}", String.format("%.2f", item.sellPrice)),
                net.lunark.io.language.LanguageManager.ComponentPlaceholder.of("{symbol}", config.currencySymbol)));

        refreshGUI(player);
    }

    private void refreshGUI(Player player) {
        String section = openSection.get(player.getUniqueId());
        int page = currentPage.getOrDefault(player.getUniqueId(), 1);
        if (section != null) {
            Bukkit.getScheduler().runTask(plugin, () -> openSectionGUI(player, section, page));
        }
    }

    public void refreshOpenInventories() {
        new HashSet<>(openSection.keySet()).forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.getOpenInventory().getTopInventory().getHolder() == null) {
                String section = openSection.get(uuid);
                int page = currentPage.getOrDefault(uuid, 1);
                if (section == null) {
                    Bukkit.getScheduler().runTask(plugin, () -> openMainGUI(player));
                } else {
                    Bukkit.getScheduler().runTask(plugin, () -> openSectionGUI(player, section, page));
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
                    net.lunark.io.language.LanguageManager.ComponentPlaceholder.of("{balance}",
                            String.format("%.2f", economy.getBalance(player)))));
            skull.setItemMeta(meta);
        }
        return skull;
    }
}