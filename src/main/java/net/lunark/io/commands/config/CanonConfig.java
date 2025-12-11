package net.lunark.io.commands.config;

import org.bukkit.plugin.Plugin;

public final class CanonConfig {
    private final double velocity;

    public CanonConfig(Plugin plugin) {
        this.velocity = plugin.getConfig().getDouble("canon.velocity", 2.0);
    }

    public double getVelocity() {
        return velocity;
    }
}