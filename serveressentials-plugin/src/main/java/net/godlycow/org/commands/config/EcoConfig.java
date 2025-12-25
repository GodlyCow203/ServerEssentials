package net.godlycow.org.commands.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

/**
 * Configuration for Economy Management Commands (/eco)
 * Supports both Vault economy and internal fallback economy
 */
public final class EcoConfig {
    private final Plugin plugin;
    public final boolean enabled;
    public final double maxTransactionAmount;
    public final boolean notifyTarget;
    public final boolean logTransactions;

    public EcoConfig(Plugin plugin) {
        this.plugin = plugin;

        // Load configuration from config.yml
        FileConfiguration config = plugin.getConfig();

        this.enabled = config.getBoolean("eco.enabled", true);
        this.maxTransactionAmount = config.getDouble("eco.max-transaction-amount", 1000000.0);
        this.notifyTarget = config.getBoolean("eco.notify-target", true);
        this.logTransactions = config.getBoolean("eco.log-transactions", true);

        // Log loaded values for debugging
        plugin.getLogger().info("[Eco] Max transaction amount: " + maxTransactionAmount);
        plugin.getLogger().info("[Eco] Notify target: " + notifyTarget);
        plugin.getLogger().info("[Eco] Log transactions: " + logTransactions);
    }

    /**
     * Reloads the configuration
     */
    public void reload() {
        plugin.reloadConfig();
        // In a real implementation, you would reload values here
        plugin.getLogger().info("[Eco] Configuration reloaded");
    }
}