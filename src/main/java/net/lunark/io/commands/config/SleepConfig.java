package net.lunark.io.commands.config;

import org.bukkit.plugin.Plugin;

public final class SleepConfig {
    private final long timeToSet;
    private final boolean clearWeather;
    private final int weatherDuration;
    private final boolean broadcastMessage;

    public SleepConfig(Plugin plugin) {
        this.timeToSet = plugin.getConfig().getLong("sleep.time-to-set", 0L);
        this.clearWeather = plugin.getConfig().getBoolean("sleep.clear-weather", true);
        this.weatherDuration = plugin.getConfig().getInt("sleep.weather-duration", 6000);
        this.broadcastMessage = plugin.getConfig().getBoolean("sleep.broadcast-message", true);
    }

    public long timeToSet() { return timeToSet; }
    public boolean clearWeather() { return clearWeather; }
    public int weatherDuration() { return weatherDuration; }
    public boolean broadcastMessage() { return broadcastMessage; }
}