package net.lunark.io.commands.config;

import org.bukkit.plugin.Plugin;

public final class MagnetConfig {
    public final double radius;
    public final double speed;
    public final int tickInterval;

    public MagnetConfig(Plugin plugin) {
        this.radius = plugin.getConfig().getDouble("magnet.radius", 5.0);
        this.speed = plugin.getConfig().getDouble("magnet.speed", 0.5);
        this.tickInterval = plugin.getConfig().getInt("magnet.tick-interval", 10);
    }
}