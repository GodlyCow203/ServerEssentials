package net.lunark.io.commands.config;

import org.bukkit.plugin.Plugin;


public final class RepairConfig {
    private final boolean playSound;
    private final boolean requirePermissionEachUse;

    public RepairConfig(Plugin plugin) {
        this.playSound = plugin.getConfig().getBoolean("repair.play-sound", true);
        this.requirePermissionEachUse = plugin.getConfig().getBoolean("repair.require-permission-each-use", false);
    }

    public boolean playSound() { return playSound; }
    public boolean requirePermissionEachUse() { return requirePermissionEachUse; }
}