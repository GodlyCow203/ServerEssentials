package net.godlycow.org.commands.config;

import org.bukkit.plugin.Plugin;

public final class LaunchConfig {
    private final double launchPower;
    private final double launchHeight;

    public LaunchConfig(Plugin plugin) {
        this.launchPower = plugin.getConfig().getDouble("launch.power", 2.0);
        this.launchHeight = plugin.getConfig().getDouble("launch.height", 1.5);
    }

    public double launchPower() { return launchPower; }
    public double launchHeight() { return launchHeight; }
}