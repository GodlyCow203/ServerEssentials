package net.godlycow.org.commands.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;


public final class EcoConfig {
    private final Plugin plugin;
    public final boolean enabled;
    public final double maxTransactionAmount;
    public final boolean notifyTarget;
    public final boolean logTransactions;

    public EcoConfig(Plugin plugin) {
        this.plugin = plugin;

        FileConfiguration config = plugin.getConfig();

        this.enabled = config.getBoolean("eco.enabled", true);
        this.maxTransactionAmount = config.getDouble("eco.max-transaction-amount", 1000000.0);
        this.notifyTarget = config.getBoolean("eco.notify-target", true);
        this.logTransactions = config.getBoolean("eco.log-transactions", true);
    }


    public void reload() {
        plugin.reloadConfig();
        plugin.getLogger().info("[Eco] Configuration reloaded");
    }
}