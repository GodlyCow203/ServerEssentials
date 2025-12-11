package net.lunark.io.commands.config;

import org.bukkit.plugin.Plugin;
import java.time.Duration;

public class MuteConfig {
    private final Plugin plugin;

    public final Duration checkInterval;

    public MuteConfig(Plugin plugin) {
        this.plugin = plugin;
        this.checkInterval = Duration.ofMinutes(plugin.getConfig().getLong("mute.check-interval-minutes", 1L));
    }
}