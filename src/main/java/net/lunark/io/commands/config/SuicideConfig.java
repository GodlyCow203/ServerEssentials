package net.lunark.io.commands.config;

import org.bukkit.plugin.Plugin;

/**
 * Configuration for /suicide command
 * Permission: serveressentials.command.suicide (hardcoded)
 */
public final class SuicideConfig {
    // Reserved for future options like cooldown, confirmation, etc.

    public SuicideConfig(Plugin plugin) {
        // Example: this.requireConfirmation = plugin.getConfig().getBoolean("suicide.confirm", false);
    }
}