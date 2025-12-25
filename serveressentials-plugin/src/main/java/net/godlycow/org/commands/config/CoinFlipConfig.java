package net.godlycow.org.commands.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

/**
 * Configuration for Coin Flip command
 * Supports both Vault economy and internal fallback economy
 */
public final class CoinFlipConfig {
    private final Plugin plugin;
    public final boolean enabled;
    public final double minBetAmount;
    public final double maxBetAmount;
    public final boolean logFlips;
    public final double winChance; // Optional: configurable win chance

    public CoinFlipConfig(Plugin plugin) {
        this.plugin = plugin;

        // Load configuration from config.yml
        FileConfiguration config = plugin.getConfig();

        this.enabled = config.getBoolean("coinflip.enabled", true);
        this.minBetAmount = config.getDouble("coinflip.min-bet-amount", 1.0);
        this.maxBetAmount = config.getDouble("coinflip.max-bet-amount", 10000.0);
        this.logFlips = config.getBoolean("coinflip.log-flips", true);
        this.winChance = config.getDouble("coinflip.win-chance", 0.5); // 50% default

        // Validate configuration
        if (minBetAmount < 0) {
            plugin.getLogger().warning("[CoinFlip] min-bet-amount cannot be negative, using default 1.0");
        }
        if (maxBetAmount < minBetAmount) {
            plugin.getLogger().warning("[CoinFlip] max-bet-amount must be greater than min-bet-amount");
        }
        if (winChance < 0.0 || winChance > 1.0) {
            plugin.getLogger().warning("[CoinFlip] win-chance must be between 0.0 and 1.0, using default 0.5");
        }

        plugin.getLogger().info("[CoinFlip] Loaded config: min=" + minBetAmount +
                ", max=" + maxBetAmount +
                ", winChance=" + (winChance * 100) + "%");
    }
}