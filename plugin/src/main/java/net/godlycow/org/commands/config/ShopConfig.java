package net.godlycow.org.commands.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import java.io.File;

public final class ShopConfig {
    private final Plugin plugin;
    public final boolean enabled;
    public final String mainTitle;
    public final int mainSize;
    public final int closeButtonSlot;
    public final int playerHeadSlot;
    public final boolean enableSell;
    public final boolean enableBulkBuy;
    public final boolean enableBulkSell;
    public final boolean enableStock;
    public final boolean enableSounds;
    public final boolean enableAnimations;
    public final int maxBulkAmount;
    public final int clickCooldownMs;
    public final int maxPages;

    public ShopConfig(Plugin plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();

        this.enabled = config.getBoolean("shop.enabled", true);
        this.mainTitle = config.getString("shop.main.title", "<gradient:gold:yellow>🛒 Main Shop");
        this.mainSize = config.getInt("shop.main.size", 54);
        this.closeButtonSlot = config.getInt("shop.main.close-button-slot", 49);
        this.playerHeadSlot = config.getInt("shop.section.player-head-slot", 4);
        this.enableSell = config.getBoolean("shop.economy.enable-sell", true);
        this.enableBulkBuy = config.getBoolean("shop.features.enable-bulk-buy", true);
        this.enableBulkSell = config.getBoolean("shop.features.enable-bulk-sell", true);
        this.enableStock = config.getBoolean("shop.features.enable-stock", false);
        this.enableSounds = config.getBoolean("shop.features.enable-sounds", true);
        this.enableAnimations = config.getBoolean("shop.features.enable-animations", true);
        this.maxBulkAmount = config.getInt("shop.features.max-bulk-amount", 9999);
        this.clickCooldownMs = config.getInt("shop.performance.click-cooldown-ms", 150);
        this.maxPages = config.getInt("shop.section.max-pages", 10);
    }

    public File getShopFolder() {
        return new File(plugin.getDataFolder(), "shop");
    }
}