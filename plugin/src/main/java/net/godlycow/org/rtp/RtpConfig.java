package net.godlycow.org.rtp;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RtpConfig {
    private final Plugin plugin;
    private final FileConfiguration config;
    private final Map<String, WorldConfig> worldConfigs = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public static class WorldConfig {
        public final boolean enabled;
        public final int minRadius;
        public final int maxRadius;
        public final int cooldown;

        public WorldConfig(boolean enabled, int minRadius, int maxRadius, int cooldown) {
            this.enabled = enabled;
            this.minRadius = minRadius;
            this.maxRadius = maxRadius;
            this.cooldown = cooldown;
        }
    }

    public RtpConfig(Plugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        load();
    }

    public void reload() {
        plugin.reloadConfig();
        load();
    }

    public void load() {
        lock.writeLock().lock();
        try {
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
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean isWorldEnabled(String worldName) {
        lock.readLock().lock();
        try {
            WorldConfig wc = worldConfigs.get(worldName);
            return wc != null && wc.enabled;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getMinRadius(String worldName) {
        lock.readLock().lock();
        try {
            WorldConfig wc = worldConfigs.get(worldName);
            return wc != null ? wc.minRadius : 1000;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getMaxRadius(String worldName) {
        lock.readLock().lock();
        try {
            WorldConfig wc = worldConfigs.get(worldName);
            return wc != null ? wc.maxRadius : 5000;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getCooldown(String worldName) {
        lock.readLock().lock();
        try {
            WorldConfig wc = worldConfigs.get(worldName);
            return wc != null ? wc.cooldown : 300;
        } finally {
            lock.readLock().unlock();
        }
    }
}