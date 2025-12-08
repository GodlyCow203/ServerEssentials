package net.lunark.io.commands.config;

import org.bukkit.plugin.Plugin;


public final class FeedConfig {
    private final int foodLevel;
    private final int saturation;

    public FeedConfig(Plugin plugin) {
        this.foodLevel = plugin.getConfig().getInt("feed.food-level", 20);
        this.saturation = plugin.getConfig().getInt("feed.saturation", 20);
    }

    public int foodLevel() { return foodLevel; }
    public int saturation() { return saturation; }
}