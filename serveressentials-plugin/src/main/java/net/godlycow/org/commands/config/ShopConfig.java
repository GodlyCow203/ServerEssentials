package net.godlycow.org.commands.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

public final class ShopConfig {
    private final Plugin plugin;
    public final String mainTitle;
    public final int mainSize;
    public final int closeButtonSlot;
    public final int defaultSectionSize;
    public final int playerHeadSlot;
    public final int maxPages;
    public final String currencySymbol;
    public final boolean enableSell;
    public final boolean enabled;

    public ShopConfig(Plugin plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();

        this.enabled = config.getBoolean("shop.enabled", true);
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