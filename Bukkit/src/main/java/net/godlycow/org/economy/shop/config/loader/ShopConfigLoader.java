package net.godlycow.org.economy.shop.config.loader;

import net.godlycow.org.economy.shop.ShopDataManager;
import net.godlycow.org.economy.shop.config.MainShopConfig;
import net.godlycow.org.economy.shop.config.ShopSectionConfig;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ShopConfigLoader {

    private static final com.google.gson.Gson gson = new com.google.gson.Gson();

    public static MainShopConfig loadMainConfig(File file, ShopDataManager dataManager) {
        // Check DB first - it should be the source of truth
        MainShopConfig dbConfig = dataManager.loadMainConfig().join();
        if (dbConfig != null && isValidMainConfig(dbConfig)) {
            return dbConfig;
        }

        // Only load from file if DB is empty/invalid
        if (!file.exists()) {
            System.err.println("No main.yml found. Using defaults.");
            return new MainShopConfig();
        }

        System.out.println("Loading main config from file (DB empty)");
        return loadMainConfigFromFileInternal(file);
    }

    public static ShopSectionConfig loadSectionConfig(File file, String sectionName, ShopDataManager dataManager) {
        // Check DB first
        ShopSectionConfig dbConfig = dataManager.loadSectionConfig(sectionName).join();
        if (dbConfig != null && isValidSectionConfig(dbConfig)) {
            return dbConfig;
        }

        // Load from file if DB empty
        if (!file.exists()) {
            System.err.println("Section file not found: " + file.getName());
            return new ShopSectionConfig();
        }

        System.out.println("Loading section '" + sectionName + "' from file (DB empty)");
        return loadSectionConfigFromFileInternal(file);
    }

    // Internal file loading methods - DO NOT SAVE TO DB
    private static MainShopConfig loadMainConfigFromFileInternal(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
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

    private static ShopSectionConfig loadSectionConfigFromFileInternal(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
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
            ConfigurationSection itemsSection = config.getConfigurationSection("items");
            for (String key : itemsSection.getKeys(false)) {
                ConfigurationSection itemSec = itemsSection.getConfigurationSection(key);
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

    // Validation helpers
    private static boolean isValidMainConfig(MainShopConfig config) {
        return config != null && config.size > 0 && config.layout != null && config.sectionButtons != null;
    }

    private static boolean isValidSectionConfig(ShopSectionConfig config) {
        return config != null && config.size > 0 && config.layout != null && config.items != null;
    }

    public static void saveMainConfig(File file, MainShopConfig main) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        config.set("title", main.title);
        config.set("size", main.size);

        config.set("layout", null);
        main.layout.forEach((slot, item) -> {
            config.set("layout." + slot + ".material", item.material.name());
            config.set("layout." + slot + ".name", item.name);
            config.set("layout." + slot + ".clickable", item.clickable);
        });

        config.set("sections", null);
        main.sectionButtons.forEach((slot, button) -> {
            config.set("sections." + slot + ".material", button.material.name());
            config.set("sections." + slot + ".name", button.name);
            config.set("sections." + slot + ".lore", button.lore);
            config.set("sections." + slot + ".file", button.file);
        });

        try {
            config.save(file);
        } catch (Exception e) {
            System.err.println("Failed to save main config: " + e.getMessage());
        }
    }

    public static void saveSectionConfig(File file, ShopSectionConfig section) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        config.set("title", section.title);
        config.set("size", section.size);
        config.set("pages", section.pages);
        config.set("player-head-slot", section.playerHeadSlot);
        config.set("close-button-slot", section.closeButtonSlot);

        config.set("layout", null);
        section.layout.forEach((slot, item) -> {
            config.set("layout." + slot + ".material", item.material.name());
            config.set("layout." + slot + ".name", item.name);
            config.set("layout." + slot + ".lore", item.lore);
            config.set("layout." + slot + ".clickable", item.clickable);
        });

        config.set("items", null);
        section.items.forEach((key, item) -> {
            config.set("items." + key + ".material", item.material.name());
            config.set("items." + key + ".amount", item.amount);
            config.set("items." + key + ".name", item.name);
            config.set("items." + key + ".lore", item.lore);
            config.set("items." + key + ".slot", item.slot);
            config.set("items." + key + ".page", item.page);
            config.set("items." + key + ".buy-price", item.buyPrice);
            config.set("items." + key + ".sell-price", item.sellPrice);
            config.set("items." + key + ".custom-item-id", item.customItemId);
            config.set("items." + key + ".clickable", item.clickable);
        });

        try {
            config.save(file);
        } catch (Exception e) {
            System.err.println("Failed to save section config: " + e.getMessage());
        }
    }
}