package net.godlycow.org.commands.config;

import org.bukkit.plugin.Plugin;

public final class BeezookaConfig {
    private final double velocity;

    public BeezookaConfig(Plugin plugin) {
        this.velocity = plugin.getConfig().getDouble("beezooka.velocity", 2.5);
    }

    public double getVelocity() {
        return velocity;
    }
}