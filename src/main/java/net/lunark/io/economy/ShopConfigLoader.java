package net.lunark.io.economy;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ShopConfigLoader {

    public static MainShopConfig loadMainConfig(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        MainShopConfig main = new MainShopConfig();

        main.title = config.getString("title", "Shop");
        main.size = config.getInt("size", 54);

        // Load layout
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

        // Load sections
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

    public static ShopSectionConfig loadSectionConfig(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ShopSectionConfig section = new ShopSectionConfig();

        section.title = config.getString("title", "Shop Section");
        section.size = config.getInt("size", 54);
        section.pages = config.getInt("pages", 1);
        section.playerHeadSlot = config.getInt("player-head-slot", -1);
        section.closeButtonSlot = config.getInt("close-button-slot", -1);

        // Load layout
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

        // Load items
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
}