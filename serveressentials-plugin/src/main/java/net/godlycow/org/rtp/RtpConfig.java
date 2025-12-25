package net.godlycow.org.rtp;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class RtpConfig {
    private final Plugin plugin;
    private final FileConfiguration config;
    private final Map<String, WorldConfig> worldConfigs = new HashMap<>();

    public record WorldConfig(boolean enabled, int minRadius, int maxRadius, int cooldown) {}

    public RtpConfig(Plugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        load();
    }

    public void reload() {
        plugin.reloadConfig();
    }

    public void load() {
        worldConfigs.clear();

        if (!config.contains("rtp")) {
            config.createSection("rtp");
            config.set("rtp.example_world.enabled", true);
            config.set("rtp.example_world.min_radius", 1000);
            config.set("rtp.example_world.max_radius", 5000);
            config.set("rtp.example_world.cooldown_seconds", 300);
            plugin.saveConfig();
        }

        for (String worldName : config.getConfigurationSection("rtp").getKeys(false)) {
            String path = "rtp." + worldName + ".";
            boolean enabled = config.getBoolean(path + "enabled", false);
            int min = config.getInt(path + "min_radius", 1000);
            int max = config.getInt(path + "max_radius", 5000);
            int cooldown = config.getInt(path + "cooldown_seconds", 300);

            worldConfigs.put(worldName, new WorldConfig(enabled, min, max, cooldown));
        }
    }

    public boolean isWorldEnabled(String worldName) {
        return worldConfigs.getOrDefault(worldName, new WorldConfig(false, 0, 0, 0)).enabled();
    }

    public int getMinRadius(String worldName) {
        return worldConfigs.getOrDefault(worldName, new WorldConfig(false, 1000, 5000, 300)).minRadius();
    }

    public int getMaxRadius(String worldName) {
        return worldConfigs.getOrDefault(worldName, new WorldConfig(false, 1000, 5000, 300)).maxRadius();
    }

    public int getCooldown(String worldName) {
        return worldConfigs.getOrDefault(worldName, new WorldConfig(false, 1000, 5000, 300)).cooldown();
    }
}