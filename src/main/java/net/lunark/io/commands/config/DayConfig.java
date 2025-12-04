package net.lunark.io.commands.config;

import org.bukkit.plugin.Plugin;

/**
 * Type-safe configuration for /day command
 * Permission is hardcoded: serveressentials.command.day
 */
public final class DayConfig {
    // Reserved for future configuration options
    // Example: public final boolean broadcast;

    public DayConfig(Plugin plugin) {
        // broadcast = plugin.getConfig().getBoolean("day.broadcast", false);
    }
}