package net.godlycow.org.commands.config;

import org.bukkit.plugin.Plugin;
import java.util.logging.Level;

public class AuctionConfig {
    private final Plugin plugin;
    public final boolean enabled;
    public final int guiRows;
    public final int itemsPerPage;
    public final int maxSellLimit;
    public final int expirationDays;
    public final double maxPriceLimit;
    public final int maxItemsPerPlayer;

    public AuctionConfig(Plugin plugin) {
        this.plugin = plugin;

        this.enabled = getConfigBoolean("auction.enabled", true);
        this.guiRows = getConfigInt("auction.gui.rows", 6);
        this.itemsPerPage = getConfigInt("auction.items-per-page", 36);
        this.maxSellLimit = getConfigInt("auction.max-sell-limit", 64);
        this.expirationDays = getConfigInt("auction.expiration-days", 7);
        this.maxPriceLimit = getConfigDouble("auction.max-price-limit", 1000000.0);
        this.maxItemsPerPlayer = getConfigInt("auction.max-items-per-player", 10);

        validateConfig();
    }

    private boolean getConfigBoolean(String path, boolean defaultValue) {
        if (!plugin.getConfig().isSet(path)) {
            plugin.getLogger().log(Level.WARNING, "Auction config missing: " + path + ", using default: " + defaultValue);
            return defaultValue;
        }
        return plugin.getConfig().getBoolean(path, defaultValue);
    }

    private int getConfigInt(String path, int defaultValue) {
        if (!plugin.getConfig().isSet(path)) {
            plugin.getLogger().log(Level.WARNING, "Auction config missing: " + path + ", using default: " + defaultValue);
            return defaultValue;
        }
        return plugin.getConfig().getInt(path, defaultValue);
    }

    private double getConfigDouble(String path, double defaultValue) {
        if (!plugin.getConfig().isSet(path)) {
            plugin.getLogger().log(Level.WARNING, "Auction config missing: " + path + ", using default: " + defaultValue);
            return defaultValue;
        }
        return plugin.getConfig().getDouble(path, defaultValue);
    }

    private void validateConfig() {
        if (guiRows < 1 || guiRows > 6) {
            plugin.getLogger().log(Level.WARNING, "Invalid auction.gui.rows: " + guiRows + ", must be 1-6. Using default 6.");
        }

        if (itemsPerPage > (guiRows - 1) * 7) {
            plugin.getLogger().log(Level.WARNING, "items-per-page (" + itemsPerPage + ") exceeds available slots for guiRows (" + guiRows + ")");
        }
    }
}