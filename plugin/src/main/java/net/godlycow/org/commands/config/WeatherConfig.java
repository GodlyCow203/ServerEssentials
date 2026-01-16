package net.godlycow.org.commands.config;

import org.bukkit.plugin.Plugin;

public final class WeatherConfig {
    private final int durationTicks;
    private final boolean notifyWorld;

    public WeatherConfig(Plugin plugin) {
        this.durationTicks = plugin.getConfig().getInt("weather.duration-seconds", 600) * 20;
        this.notifyWorld = plugin.getConfig().getBoolean("weather.notify-world", false);
    }

    public int durationTicks() { return durationTicks; }
    public boolean notifyWorld() { return notifyWorld; }
}