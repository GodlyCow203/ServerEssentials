package net.lunark.io.commands.config;

import org.bukkit.plugin.Plugin;

/**
 * Configuration for /kill command
 * Permission: serveressentials.command.kill (hardcoded)
 */
public final class KillConfig {
    // Reserved for future options like showing global rank, stats, etc.

    public KillConfig(Plugin plugin) {
        // Example: this.showGlobalRank = plugin.getConfig().getBoolean("kill.show-rank", true);
    }
}