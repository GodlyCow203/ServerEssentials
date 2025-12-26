package net.godlycow.org.commands.config;

import org.bukkit.plugin.Plugin;

public final class KittyCannonConfig {
    private final double velocity;

    public KittyCannonConfig(Plugin plugin) {
        this.velocity = plugin.getConfig().getDouble("kittycannon.velocity", 2.0);
    }

    public double getVelocity() {
        return velocity;
    }
}