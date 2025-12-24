package net.godlycow.org.commands.config;

import org.bukkit.plugin.Plugin;

/**
 * Configuration for /break command
 * Permission: serveressentials.command.break (hardcoded)
 */
public final class BreakConfig {
    private final int maxDistance;

    public BreakConfig(Plugin plugin) {
        this.maxDistance = plugin.getConfig().getInt("break.max-distance", 5);
    }

    public int maxDistance() {
        return maxDistance;
    }
}