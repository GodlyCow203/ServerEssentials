package net.lunark.io.commands.config;

import org.bukkit.plugin.Plugin;

public final class PTimeConfig {
    private final long dayTime;
    private final long nightTime;

    public PTimeConfig(Plugin plugin) {
        this.dayTime = plugin.getConfig().getLong("ptime.day-time", 1000L);
        this.nightTime = plugin.getConfig().getLong("ptime.night-time", 13000L);
    }

    public long dayTime() { return dayTime; }
    public long nightTime() { return nightTime; }
}