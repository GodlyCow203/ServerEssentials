package net.lunark.io.commands.config;

import org.bukkit.plugin.Plugin;

/**
 * Type-safe configuration for /inventorysort command
 * Permission is hardcoded: serveressentials.command.inventorysort
 */
public final class InventorySortConfig {
    // Reserved for future options (e.g., sort-method, stack-only mode)

    public InventorySortConfig(Plugin plugin) {
        // Example: this.stackOnly = plugin.getConfig().getBoolean("inventorysort.stack-only", false);
    }
}