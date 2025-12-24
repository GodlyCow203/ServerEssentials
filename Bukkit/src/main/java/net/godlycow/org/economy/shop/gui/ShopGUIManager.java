package net.godlycow.org.economy.shop.gui;

import net.godlycow.org.economy.eco.EconomyManager;
import net.godlycow.org.economy.eco.EconomyResponse;
import net.godlycow.org.economy.shop.config.MainShopConfig;
import net.godlycow.org.economy.shop.ShopDataManager;
import net.godlycow.org.economy.shop.config.ShopSectionConfig;
import net.godlycow.org.economy.shop.storage.ShopStorage;
import net.godlycow.org.language.LanguageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.godlycow.org.commands.config.ShopConfig;
import net.godlycow.org.language.PlayerLanguageManager;
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
    private final EconomyManager economyManager;
    private final ShopDataManager dataManager;

    private final Map<String, ShopSectionConfig> sectionCache = new ConcurrentHashMap<>();
    private MainShopConfig mainConfigCache;
    private boolean configsLoaded = false;

    private final Map<UUID, String> openSection = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> currentPage = new ConcurrentHashMap<>();
    private final Map<UUID, Long> clickCooldowns = new ConcurrentHashMap<>();
    private final Set<UUID> mainGUIOpen = ConcurrentHashMap.newKeySet();

    public ShopGUIManager(Plugin plugin, PlayerLanguageManager langManager,
                          ShopStorage storage, ShopConfig config,
                          EconomyManager economyManager, ShopDataManager dataManager) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.storage = storage;
        this.config = config;
        this.economyManager = economyManager;
        this.dataManager = dataManager;

        loadAllConfigs(false);

        if (!economyManager.isEnabled()) {
            plugin.getLogger().warning("§eShopGUIManager: Economy disabled! Shop will be view-only.");
        }
    }

    private void loadAllConfigs() {
        loadAllConfigs(false);
    }

    private void loadAllConfigs(boolean forceFromFiles) {
        // Step 1: Check if shop folder exists
        File shopFolder = config.getShopFolder();
        if (!shopFolder.exists() || !shopFolder.isDirectory()) {
            plugin.getLogger().warning("Shop folder not found at: " + shopFolder.getPath());
            mainConfigCache = new MainShopConfig();
            configsLoaded = true;
            return;
        }

        // Step 2: Load main config with smart source selection
        loadMainConfigSmart(forceFromFiles);

        // Step 3: Load section configs
        loadSectionConfigsSmart(forceFromFiles);

        configsLoaded = true;
        plugin.getLogger().info("✓ Shop configurations loaded successfully");
    }

    private void loadMainConfigSmart(boolean forceFromFiles) {
        File mainFile = new File(config.getShopFolder(), "main.yml");

        // Get timestamps for comparison
        long fileModified = mainFile.exists() ? mainFile.lastModified() : 0;
        long dbUpdated = dataManager.getMainConfigLastUpdate().join();

        boolean shouldLoadFromFile = forceFromFiles ||
                (fileModified > dbUpdated && fileModified > 0) ||
                dbUpdated == 0;

        if (shouldLoadFromFile && mainFile.exists()) {
            // Load from file and validate
            mainConfigCache = loadMainConfigFromFileInternal(mainFile);
            if (isValidMainConfig(mainConfigCache)) {
                dataManager.saveMainConfig(mainConfigCache);
                plugin.getLogger().info("✓ Loaded main shop config from file (newer than DB)");
            } else {
                plugin.getLogger().severe("§cLoaded main config from file but it's invalid! Using DB fallback.");
                fallbackToDBMain();
            }
        } else if (dbUpdated > 0) {
            // Load from database
            fallbackToDBMain();
        } else {
            // No valid config found anywhere
            plugin.getLogger().warning("§eNo main shop config found in files or database. Using defaults.");
            mainConfigCache = new MainShopConfig();
        }
    }

    private void fallbackToDBMain() {
        MainShopConfig dbConfig = dataManager.loadMainConfig().join();
        if (isValidMainConfig(dbConfig)) {
            mainConfigCache = dbConfig;
            plugin.getLogger().info("✓ Loaded main shop config from database");
        } else {
            plugin.getLogger().severe("§cDatabase main config is invalid! Creating new default.");
            mainConfigCache = new MainShopConfig();
        }
    }

    private void loadSectionConfigsSmart(boolean forceFromFiles) {
        File shopFolder = config.getShopFolder();
        File[] files = shopFolder.listFiles((d, name) -> name.endsWith(".yml") && !name.equals("main.yml"));

        if (files == null || files.length == 0) {
            plugin.getLogger().info("No section config files found.");
            return;
        }

        Set<String> processedSections = new HashSet<>();
        for (File file : files) {
            String name = file.getName().replace(".yml", "");
            processedSections.add(name);
            loadSingleSectionSmart(name, forceFromFiles);
        }

        // Clean up sections that no longer have files
        sectionCache.keySet().retainAll(processedSections);
    }

    private void loadSingleSectionSmart(String sectionName, boolean forceFromFiles) {
        File sectionFile = new File(config.getShopFolder(), sectionName + ".yml");

        // Skip if file doesn't exist
        if (!sectionFile.exists()) {
            sectionCache.remove(sectionName);
            return;
        }

        long fileModified = sectionFile.lastModified();
        long dbUpdated = dataManager.getSectionLastUpdate(sectionName).join();

        boolean shouldLoadFromFile = forceFromFiles ||
                (fileModified > dbUpdated && fileModified > 0) ||
                dbUpdated == 0;

        if (shouldLoadFromFile) {
            ShopSectionConfig section = loadSectionConfigFromFileInternal(sectionFile);
            if (isValidSectionConfig(section)) {
                sectionCache.put(sectionName, section);
                dataManager.saveSectionConfig(sectionName, section);
                plugin.getLogger().info("✓ Loaded section '" + sectionName + "' from file");
            } else {
                plugin.getLogger().severe("§cSection '" + sectionName + "' loaded from file is invalid! Using DB fallback.");
                fallbackToDBSection(sectionName);
            }
        } else if (dbUpdated > 0) {
            fallbackToDBSection(sectionName);
        }
    }

    private void fallbackToDBSection(String sectionName) {
        ShopSectionConfig dbSection = dataManager.loadSectionConfig(sectionName).join();
        if (isValidSectionConfig(dbSection)) {
            sectionCache.put(sectionName, dbSection);
            plugin.getLogger().info("✓ Loaded section '" + sectionName + "' from database");
        } else {
            plugin.getLogger().warning("§eSection '" + sectionName + "' not found in database. Skipping.");
            sectionCache.remove(sectionName);
        }
    }

    // Internal file loading methods - DO NOT SAVE TO DB
    private MainShopConfig loadMainConfigFromFileInternal(File file) {
        org.bukkit.configuration.file.YamlConfiguration config =
                org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
        MainShopConfig main = new MainShopConfig();

        main.title = config.getString("title", "Shop");
        main.size = config.getInt("size", 54);

        if (config.isConfigurationSection("layout")) {
            for (String key : config.getConfigurationSection("layout").getKeys(false)) {
                int slot = Integer.parseInt(key);
                MainShopConfig.LayoutItem item = new MainShopConfig.LayoutItem();
                item.material = Material.valueOf(config.getString("layout." + key + ".material", "STONE"));
                item.name = config.getString("layout." + key + ".name", "");
                item.clickable = config.getBoolean("layout." + key + ".clickable", false);
                main.layout.put(slot, item);
            }
        }

        if (config.isConfigurationSection("sections")) {
            for (String key : config.getConfigurationSection("sections").getKeys(false)) {
                int slot = Integer.parseInt(key);
                MainShopConfig.SectionButton button = new MainShopConfig.SectionButton();
                button.material = Material.valueOf(config.getString("sections." + key + ".material", "STONE"));
                button.name = config.getString("sections." + key + ".name", "");
                button.lore = config.getStringList("sections." + key + ".lore");
                button.file = config.getString("sections." + key + ".file");
                main.sectionButtons.put(slot, button);
            }
        }

        return main;
    }

    private ShopSectionConfig loadSectionConfigFromFileInternal(File file) {
        org.bukkit.configuration.file.YamlConfiguration config =
                org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
        ShopSectionConfig section = new ShopSectionConfig();

        section.title = config.getString("title", "Shop Section");
        section.size = config.getInt("size", 54);
        section.pages = config.getInt("pages", 1);
        section.playerHeadSlot = config.getInt("player-head-slot", -1);
        section.closeButtonSlot = config.getInt("close-button-slot", -1);

        if (config.isConfigurationSection("layout")) {
            config.getConfigurationSection("layout").getKeys(false).forEach(key -> {
                int slot = Integer.parseInt(key);
                ShopSectionConfig.LayoutItem item = new ShopSectionConfig.LayoutItem();
                item.material = Material.valueOf(config.getString("layout." + key + ".material", "STONE"));
                item.name = config.getString("layout." + key + ".name", "");
                item.lore = config.getStringList("layout." + key + ".lore");
                item.clickable = config.getBoolean("layout." + key + ".clickable", false);
                section.layout.put(slot, item);
            });
        }

        if (config.isConfigurationSection("items")) {
            org.bukkit.configuration.ConfigurationSection itemsSection = config.getConfigurationSection("items");
            for (String key : itemsSection.getKeys(false)) {
                org.bukkit.configuration.ConfigurationSection itemSec = itemsSection.getConfigurationSection(key);
                if (itemSec == null) continue;

                ShopSectionConfig.ShopItem item = new ShopSectionConfig.ShopItem();
                item.material = Material.matchMaterial(itemSec.getString("material", "STONE"));
                item.amount = itemSec.getInt("amount", 1);
                item.name = itemSec.getString("name", "");
                item.lore = itemSec.getStringList("lore");
                item.slot = itemSec.getInt("slot", -1);
                item.page = itemSec.getInt("page", 1);
                item.buyPrice = itemSec.getDouble("buy-price", -1);
                item.sellPrice = itemSec.getDouble("sell-price", -1);
                item.customItemId = itemSec.getString("custom-item-id", null);
                item.clickable = itemSec.getBoolean("clickable", true);

                section.items.put(key, item);
            }
        }

        return section;
    }

    // Validation methods
    private boolean isValidMainConfig(MainShopConfig config) {
        return config != null &&
                config.size > 0 &&
                config.layout != null &&
                config.sectionButtons != null &&
                config.title != null;
    }

    private boolean isValidSectionConfig(ShopSectionConfig config) {
        return config != null &&
                config.size > 0 &&
                config.layout != null &&
                config.items != null &&
                config.title != null;
    }

    public void openMainGUI(Player player) {
        if (!configsLoaded) {
            player.sendMessage(langManager.getMessageFor(player, "economy.shop.loading",
                    "<yellow>Loading shop...</yellow>"));
            return;
        }

        if (mainConfigCache == null) {
            player.sendMessage(langManager.getMessageFor(player, "economy.shop.no-main-config",
                    "<red>Shop configuration not found. Please contact an administrator."));
            return;
        }

        Component title = langManager.getMessageFor(player, "economy.shop.main-title",
                mainConfigCache.title != null ? mainConfigCache.title : "<green>Main Shop");

        Inventory inv = Bukkit.createInventory(null, config.mainSize, title);

        for (Map.Entry<Integer, MainShopConfig.LayoutItem> entry : mainConfigCache.layout.entrySet()) {
            int slot = entry.getKey();
            MainShopConfig.LayoutItem item = entry.getValue();
            ItemStack stack = createItem(item.material, item.name, null, 1);
            inv.setItem(slot, stack);
        }

        for (Map.Entry<Integer, MainShopConfig.SectionButton> entry : mainConfigCache.sectionButtons.entrySet()) {
            int slot = entry.getKey();
            MainShopConfig.SectionButton button = entry.getValue();
            ItemStack stack = createItem(button.material, button.name, button.lore, 1);
            inv.setItem(slot, stack);
        }

        ItemStack close = createItem(Material.BARRIER, "<red>Close", null, 1);
        inv.setItem(config.closeButtonSlot, close);

        player.openInventory(inv);

        openSection.remove(player.getUniqueId());
        mainGUIOpen.add(player.getUniqueId());
    }

    public void openSectionGUI(Player player, String fileName, int page) {
        if (!configsLoaded) {
            player.sendMessage(langManager.getMessageFor(player, "economy.shop.loading",
                    "<yellow>Loading shop...</yellow>"));
            return;
        }

        ShopSectionConfig section = sectionCache.get(fileName);
        if (section == null) {
            player.sendMessage(langManager.getMessageFor(player, "economy.shop.no-section",
                    "<red>Shop section not found: <white>{section}",
                    LanguageManager.ComponentPlaceholder.of("{section}", fileName)));
            return;
        }

        String displayTitle = section.title != null ? section.title :
                fileName.replace(".yml", "").replace("-", " ");
        Component title = langManager.getMessageFor(player, "economy.shop.section-title",
                "<green>{section} Shop",
                LanguageManager.ComponentPlaceholder.of("{section}", displayTitle));

        Inventory inv = Bukkit.createInventory(null, section.size, title);

        for (Map.Entry<Integer, ShopSectionConfig.LayoutItem> entry : section.layout.entrySet()) {
            int slot = entry.getKey();
            ShopSectionConfig.LayoutItem item = entry.getValue();
            ItemStack stack = createItem(item.material, item.name, item.lore, 1);
            inv.setItem(slot, stack);
        }

        section.items.values().stream()
                .filter(item -> item.page == page)
                .forEach(item -> {
                    ItemStack stack = createItem(item.material, item.name, item.lore, item.amount);

                    if (economyManager.isEnabled()) {
                        List<String> itemLore = new ArrayList<>(item.lore != null ? item.lore : new ArrayList<>());

                        if (item.buyPrice > 0) {
                            itemLore.add("<green>L-Click: Buy for " + economyManager.format(item.buyPrice));
                        }
                        if (item.sellPrice > 0 && config.enableSell) {
                            itemLore.add("<yellow>R-Click: Sell for " + economyManager.format(item.sellPrice));
                        }

                        stack = createItem(item.material, item.name, itemLore, item.amount);
                    } else {
                        List<String> disabledLore = new ArrayList<>();
                        disabledLore.add("<red>§c✗ Economy not available");
                        disabledLore.add("<gray>Shop is view-only");
                        stack = createItem(item.material, item.name, disabledLore, item.amount);
                    }

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
        mainGUIOpen.remove(player.getUniqueId());

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
        if (mainConfigCache == null) return;

        if (slot == config.closeButtonSlot) {
            player.closeInventory();
            return;
        }

        MainShopConfig.SectionButton button = mainConfigCache.sectionButtons.get(slot);
        if (button != null) {
            String sectionFileName = button.file.replace(".yml", "");
            Bukkit.getScheduler().runTask(plugin, () -> openSectionGUI(player, sectionFileName, 1));
        }
    }

    private void handleSectionClick(Player player, int slot, int page, String sectionFile, String clickType) {
        ShopSectionConfig section = sectionCache.get(sectionFile);
        if (section == null) return;

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

        if (!economyManager.isEnabled()) {
            player.sendMessage(langManager.getMessageFor(player, "economy.shop.no-economy",
                    "<red>§c✗ Economy system is not available. Shop is view-only."));
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
        if (!economyManager.isEnabled()) {
            player.sendMessage(langManager.getMessageFor(player, "economy.shop.no-economy",
                    "<red>§c✗ Economy system is not available."));
            return;
        }

        // Calculate total buy price for the amount being bought
        double totalBuyPrice = item.buyPrice * item.amount;

        double balance = economyManager.getBalance(player);
        if (balance >= totalBuyPrice) {
            EconomyResponse response = economyManager.withdraw(player, totalBuyPrice);

            if (response.success()) {
                player.getInventory().addItem(new ItemStack(item.material, item.amount));

                player.sendMessage(langManager.getMessageFor(player, "economy.shop.buy-success",
                        "<green>✓ You bought {amount}x {item} for {price}",
                        LanguageManager.ComponentPlaceholder.of("{amount}", item.amount),
                        LanguageManager.ComponentPlaceholder.of("{item}", item.name),
                        LanguageManager.ComponentPlaceholder.of("{price}", economyManager.format(totalBuyPrice))));

                refreshGUI(player);
            } else {
                player.sendMessage(langManager.getMessageFor(player, "economy.shop.transaction-failed",
                        "<red>§c✗ Transaction failed: {error}",
                        LanguageManager.ComponentPlaceholder.of("{error}", response.errorMessage)));
            }
        } else {
            player.sendMessage(langManager.getMessageFor(player, "economy.shop.cannot-afford",
                    "<red>§c✗ You cannot afford {item} (cost: {price})",
                    LanguageManager.ComponentPlaceholder.of("{item}", item.name),
                    LanguageManager.ComponentPlaceholder.of("{price}", economyManager.format(totalBuyPrice))));
        }
    }

    private void handleSell(Player player, ShopSectionConfig.ShopItem item) {
        if (!economyManager.isEnabled()) {
            player.sendMessage(langManager.getMessageFor(player, "economy.shop.no-economy",
                    "<red>§c✗ Economy system is not available."));
            return;
        }

        if (!player.getInventory().containsAtLeast(new ItemStack(item.material), item.amount)) {
            player.sendMessage(langManager.getMessageFor(player, "economy.shop.no-items",
                    "<red>§c✗ You don't have enough {item} to sell",
                    LanguageManager.ComponentPlaceholder.of("{item}", item.name)));
            return;
        }

        // Calculate total sell price for the amount being sold
        double totalSellPrice = item.sellPrice * item.amount;

        removeItems(player, item.material, item.amount);
        EconomyResponse response = economyManager.deposit(player, totalSellPrice);

        if (response.success()) {
            player.sendMessage(langManager.getMessageFor(player, "economy.shop.sell-success",
                    "<green>✓ You sold {amount}x {item} for {price}",
                    LanguageManager.ComponentPlaceholder.of("{amount}", item.amount),
                    LanguageManager.ComponentPlaceholder.of("{item}", item.name),
                    LanguageManager.ComponentPlaceholder.of("{price}", economyManager.format(totalSellPrice))));

            refreshGUI(player);
        } else {
            // Return items if transaction failed
            player.getInventory().addItem(new ItemStack(item.material, item.amount));
            player.sendMessage(langManager.getMessageFor(player, "economy.shop.transaction-failed",
                    "<red>§c✗ Transaction failed: {error}",
                    LanguageManager.ComponentPlaceholder.of("{error}", response.errorMessage)));
        }
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

    public boolean isShopGUIOpen(UUID playerUuid) {
        return mainGUIOpen.contains(playerUuid) || openSection.containsKey(playerUuid);
    }

    public void cleanupPlayer(UUID playerUuid) {
        openSection.remove(playerUuid);
        currentPage.remove(playerUuid);
        mainGUIOpen.remove(playerUuid);
        clickCooldowns.remove(playerUuid);
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

    public void reloadConfigs(boolean forceFromFiles) {
        plugin.getLogger().info("Reloading shop configurations...");

        // Clear cache first
        sectionCache.clear();
        mainConfigCache = null;
        configsLoaded = false;

        // Load fresh
        loadAllConfigs(forceFromFiles);

        // Refresh any open GUIs
        if (forceFromFiles) {
            refreshOpenInventories();
        }

        plugin.getLogger().info("✓ Reload complete (source: " +
                (forceFromFiles ? "forced from files)" : "smart load)"));
    }

    private ItemStack createPlayerHead(Player player) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(player);

            String balanceText = economyManager.isEnabled() ?
                    economyManager.format(economyManager.getBalance(player)) :
                    "N/A";

            meta.displayName(langManager.getMessageFor(player, "economy.shop.balance-display",
                    "<green>Your Balance: <gold>{balance}",
                    LanguageManager.ComponentPlaceholder.of("{balance}", balanceText)));

            if (!economyManager.isEnabled()) {
                List<Component> lore = new ArrayList<>();
                lore.add(mini.deserialize("<red>§c✗ Economy disabled"));
                meta.lore(lore);
            }

            skull.setItemMeta(meta);
        }
        return skull;
    }

    // Public getter methods
    public MainShopConfig getMainConfig() {
        return mainConfigCache;
    }

    public ShopSectionConfig getSectionConfig(String name) {
        return sectionCache.get(name);
    }

    public Map<String, ShopSectionConfig> getSectionCache() {
        return new HashMap<>(sectionCache);
    }
}