package net.godlycow.org.commands.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;


public final class CoinFlipConfig {
    private final Plugin plugin;
    public final boolean enabled;
    public final double minBetAmount;
    public final double maxBetAmount;
    public final boolean logFlips;
    public final double winChance;

    public CoinFlipConfig(Plugin plugin) {
        this.plugin = plugin;


        FileConfiguration config = plugin.getConfig();

        this.enabled = config.getBoolean("coinflip.enabled", true);
        this.minBetAmount = config.getDouble("coinflip.min-bet-amount", 1.0);
        this.maxBetAmount = config.getDouble("coinflip.max-bet-amount", 10000.0);
        this.logFlips = config.getBoolean("coinflip.log-flips", true);
        this.winChance = config.getDouble("coinflip.win-chance", 0.5);

    }
}