package net.lunark.io.economy;

import net.lunark.io.database.DatabaseManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;

/**
 * Registers ServerEssentials as Vault Economy Provider
 */
public class EconomyService {
    private final Plugin plugin;
    private final DatabaseManager databaseManager;
    private ServerEssentialsEconomy economyProvider;

    public EconomyService(Plugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    public void register() {
        ServicesManager servicesManager = plugin.getServer().getServicesManager();

        // Check if Vault is present
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().info("[Economy] Vault not found - economy provider not registered");
            return;
        }

        // Check if another economy is already registered
        if (servicesManager.isProvidedFor(Economy.class)) {
            plugin.getLogger().info("[Economy] Another economy provider detected - ServerEssentials economy not registered");
            return;
        }

        economyProvider = new ServerEssentialsEconomy(plugin, databaseManager);
        servicesManager.register(Economy.class, economyProvider, plugin, ServicePriority.Highest);
        plugin.getLogger().info("[Economy] ServerEssentials economy provider registered with Vault!");
    }



    public void unregister() {
        if (economyProvider != null) {
            plugin.getServer().getServicesManager().unregisterAll(plugin);
            plugin.getLogger().info("[Economy] ServerEssentials economy provider unregistered");
        }
    }

    public ServerEssentialsEconomy getProvider() {
        return economyProvider;
    }
}