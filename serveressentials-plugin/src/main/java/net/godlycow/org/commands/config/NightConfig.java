package net.godlycow.org.commands.config;

import org.bukkit.plugin.Plugin;

public final class NightConfig {
    private final long nightTimeTicks;

    public NightConfig(Plugin plugin) {
        this.nightTimeTicks = plugin.getConfig().getLong("night.time-ticks", 13000L);
    }

    public long nightTimeTicks() { return nightTimeTicks; }
}