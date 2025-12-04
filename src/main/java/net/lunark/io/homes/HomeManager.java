package net.lunark.io.homes;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class HomeManager {

    private final Plugin plugin;
    private final File file;
    private FileConfiguration config;

    private final Map<UUID, Map<Integer, Home>> homes = new HashMap<>();

    public HomeManager(Plugin plugin) {
        this.plugin = plugin;

        File storageFolder = new File(plugin.getDataFolder(), "storage");
        if (!storageFolder.exists()) {
            storageFolder.mkdirs();
        }

        this.file = new File(storageFolder, "homes.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create storage/homes.yml");
                e.printStackTrace();
            }
        }

        this.config = YamlConfiguration.loadConfiguration(file);
        loadAll();
    }

    public synchronized void loadAll() {
        homes.clear();
        ConfigurationSection root = config.getConfigurationSection("players");
        if (root == null) return;
        for (String uuidStr : root.getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            ConfigurationSection playerSec = root.getConfigurationSection(uuidStr);
            if (playerSec == null) continue;
            Map<Integer, Home> map = new HashMap<>();
            for (String idx : playerSec.getKeys(false)) {
                try {
                    int i = Integer.parseInt(idx);
                    ConfigurationSection hs = playerSec.getConfigurationSection(idx);
                    if (hs == null) continue;
                    Home h = new Home();
                    h.setName(hs.getString("name", "Home" + idx));
                    h.setLocation(org.bukkit.Bukkit.getWorld(hs.getString("world")) != null
                            ? new org.bukkit.Location(org.bukkit.Bukkit.getWorld(hs.getString("world")),
                            hs.getDouble("x"), hs.getDouble("y"), hs.getDouble("z"),
                            (float) hs.getDouble("yaw", 0.0), (float) hs.getDouble("pitch", 0.0))
                            : null);
                    map.put(i, h);
                } catch (Exception ignored) {}
            }
            homes.put(uuid, map);
        }
    }

    public synchronized void saveAll() {
        config.set("players", null);
        for (Map.Entry<UUID, Map<Integer, Home>> e : homes.entrySet()) {
            String uuid = e.getKey().toString();
            for (Map.Entry<Integer, Home> hentry : e.getValue().entrySet()) {
                String base = "players." + uuid + "." + hentry.getKey();
                Home h = hentry.getValue();
                config.set(base + ".name", h.getName());
                org.bukkit.Location loc = h.toLocation();
                if (loc != null) {
                    config.set(base + ".world", loc.getWorld().getName());
                    config.set(base + ".x", loc.getX());
                    config.set(base + ".y", loc.getY());
                    config.set(base + ".z", loc.getZ());
                    config.set(base + ".yaw", loc.getYaw());
                    config.set(base + ".pitch", loc.getPitch());
                }
            }
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save storage/homes.yml");
            e.printStackTrace();
        }
    }

    public synchronized Optional<Home> getHome(UUID player, int slot) {
        Map<Integer, Home> map = homes.get(player);
        if (map == null) return Optional.empty();
        Home h = map.get(slot);
        return Optional.ofNullable(h);
    }

    public synchronized void setHome(UUID player, int slot, Home home) {
        homes.computeIfAbsent(player, k -> new HashMap<>()).put(slot, home);
        saveAll();
    }

    public synchronized void removeHome(UUID player, int slot) {
        Map<Integer, Home> map = homes.get(player);
        if (map != null) {
            map.remove(slot);
            saveAll();
        }
    }

    public synchronized Map<Integer, Home> getAllHomes(UUID player) {
        return homes.getOrDefault(player, Collections.emptyMap());
    }

    public synchronized int countHomes(UUID player) {
        return homes.getOrDefault(player, Collections.emptyMap()).size();
    }
}
