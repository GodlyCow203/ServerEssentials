package net.lunark.io.economy;

import net.lunark.io.util.AnsiColorUtil;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;

public class VaultEconomyBridge {
    private final Plugin plugin;
    private final InternalEconomy economy;
    private final String name;

    public VaultEconomyBridge(Plugin plugin, InternalEconomy economy, String name) {
        this.plugin = plugin;
        this.economy = economy;
        this.name = name;
    }

    @SuppressWarnings("unchecked")
    public void register() {
        try {
            Class<?> rawEconomyClass = Class.forName("net.milkbowl.vault.economy.Economy");

            Class<Object> economyClass = (Class<Object>) rawEconomyClass;
            Object wrapper = new VaultEconomyWrapper(economy, name);

            plugin.getServer().getServicesManager().register(
                    economyClass,
                    wrapper,
                    plugin,
                    ServicePriority.Highest
            );

            plugin.getLogger().info(AnsiColorUtil.success(name + " registered with Vault for other plugins!"));
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning(AnsiColorUtil.danger("Vault API not found, skipping registration"));
        }
    }
}