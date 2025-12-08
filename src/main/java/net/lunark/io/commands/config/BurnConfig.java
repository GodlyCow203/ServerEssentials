package net.lunark.io.commands.config;

import org.bukkit.plugin.Plugin;

public final class BurnConfig {
    private final int defaultDuration;
    private final int maxDuration;

    public BurnConfig(Plugin plugin) {
        this.defaultDuration = plugin.getConfig().getInt("burn.default-duration", 5);
        this.maxDuration = plugin.getConfig().getInt("burn.max-duration", 30);
    }

    public int defaultDuration() { return defaultDuration; }
    public int maxDuration() { return maxDuration; }
}