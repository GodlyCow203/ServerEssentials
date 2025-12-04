package net.lunark.io.commands.config;

import org.bukkit.plugin.Plugin;

public final class RenameConfig {
    public final boolean allowColorCodes;
    public final int maxNameLength;

    public RenameConfig(Plugin plugin) {
        this.allowColorCodes = plugin.getConfig().getBoolean("rename.allow-color-codes", true);
        this.maxNameLength = plugin.getConfig().getInt("rename.max-name-length", 50);
    }
}