package net.lunark.io.warp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import net.lunark.io.ServerEssentials;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WarpManager {

    private final ServerEssentials plugin;
    private File file;
    private FileConfiguration config;

    private final Map<String, Location> warps = new HashMap<>();
    private final Map<String, UUID> creators = new HashMap<>();

    public WarpManager(ServerEssentials plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        file = new File(plugin.getDataFolder(), "storage/warps.yml");
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
        warps.clear();
        creators.clear();

        for (String key : config.getKeys(false)) {
            double x = config.getDouble(key + ".x");
            double y = config.getDouble(key + ".y");
            double z = config.getDouble(key + ".z");
            float yaw = (float) config.getDouble(key + ".yaw");
            float pitch = (float) config.getDouble(key + ".pitch");
            String world = config.getString(key + ".world");
            String creatorStr = config.getString(key + ".creator");

            if (world != null && Bukkit.getWorld(world) != null) {
                warps.put(key.toLowerCase(), new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch));
                if (creatorStr != null) {
                    creators.put(key.toLowerCase(), UUID.fromString(creatorStr));
                }
            }
        }
    }

    public void save() {
        for (String key : warps.keySet()) {
            Location loc = warps.get(key);
            config.set(key + ".x", loc.getX());
            config.set(key + ".y", loc.getY());
            config.set(key + ".z", loc.getZ());
            config.set(key + ".yaw", loc.getYaw());
            config.set(key + ".pitch", loc.getPitch());
            config.set(key + ".world", loc.getWorld().getName());

            UUID creator = creators.get(key.toLowerCase());
            if (creator != null) config.set(key + ".creator", creator.toString());
        }

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addWarp(String name, Location loc, UUID creator) {
        warps.put(name.toLowerCase(), loc);
        creators.put(name.toLowerCase(), creator);
        save();
    }

    public void removeWarp(String name) {
        warps.remove(name.toLowerCase());
        creators.remove(name.toLowerCase());
        config.set(name.toLowerCase(), null);
        save();
    }

    public Location getWarp(String name) {
        return warps.get(name.toLowerCase());
    }

    public UUID getCreator(String name) {
        return creators.get(name.toLowerCase());
    }

    public Map<String, Location> getWarps() {
        return warps;
    }

    public boolean exists(String name) {
        return warps.containsKey(name.toLowerCase());
    }
}
