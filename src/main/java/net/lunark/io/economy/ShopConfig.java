package net.lunark.io.economy;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopConfig {
    private final Plugin plugin;

    // Main shop settings
    public final String mainTitle;
    public final int mainSize;
    public final int closeButtonSlot;

    // Section defaults
    public final int defaultSectionSize;
    public final int playerHeadSlot;
    public final int maxPages;

    // Economy integration
    public final String currencySymbol;
    public final boolean enableSell;

    public ShopConfig(Plugin plugin) {
        this.plugin = plugin;

        FileConfiguration config = plugin.getConfig();
        this.mainTitle = config.getString("shop.main.title", "<green>Main Shop");
        this.mainSize = config.getInt("shop.main.size", 54);
        this.closeButtonSlot = config.getInt("shop.main.close-button-slot", 49);

        this.defaultSectionSize = config.getInt("shop.section.size", 54);
        this.playerHeadSlot = config.getInt("shop.section.player-head-slot", 4);
        this.maxPages = config.getInt("shop.section.max-pages", 10);

        this.currencySymbol = config.getString("shop.economy.currency-symbol", "$");
        this.enableSell = config.getBoolean("shop.economy.enable-sell", true);
    }

    public File getShopFolder() {
        return new File(plugin.getDataFolder(), "shop");
    }
}