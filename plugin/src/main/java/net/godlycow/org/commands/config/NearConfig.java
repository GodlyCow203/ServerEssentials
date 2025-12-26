package net.godlycow.org.commands.config;

import org.bukkit.plugin.Plugin;

public final class NearConfig {
    public final int maxDistance;

    public NearConfig(Plugin plugin) {
        this.maxDistance = plugin.getConfig().getInt("near.max-distance", 20);
    }
}