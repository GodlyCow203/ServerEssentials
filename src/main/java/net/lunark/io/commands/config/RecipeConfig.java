package net.lunark.io.commands.config;

import org.bukkit.plugin.Plugin;

/**
 * Configuration for /recipe command
 * Permission: serveressentials.command.recipe (hardcoded)
 */
public final class RecipeConfig {
    // Reserved for future options like showing recipe book GUI instead of chat

    public RecipeConfig(Plugin plugin) {
        // Example: this.showInGUI = plugin.getConfig().getBoolean("recipe.gui", false);
    }
}