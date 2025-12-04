package net.lunark.io.Managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class HomeManager {
    private static final Map<UUID, Map<String, Location>> homes = new HashMap<>();
    private static File file;
    private static FileConfiguration config;

    public static void loadHomes() {
        file = new File(Bukkit.getServer().getPluginManager().getPlugin("ServerEssentials").getDataFolder(), "homes.yml");
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);

        for (String uuidStr : config.getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            Map<String, Location> playerHomes = new HashMap<>();
            for (String name : config.getConfigurationSection(uuidStr).getKeys(false)) {
                playerHomes.put(name, config.getLocation(uuidStr + "." + name));
            }
            homes.put(uuid, playerHomes);
        }
    }

    public static void saveHomes() {
        for (UUID uuid : homes.keySet()) {
            for (Map.Entry<String, Location> entry : homes.get(uuid).entrySet()) {
                config.set(uuid.toString() + "." + entry.getKey(), entry.getValue());
            }
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setHome(UUID uuid, String name, Location loc) {
        homes.computeIfAbsent(uuid, k -> new HashMap<>()).put(name, loc);
        saveHomes();
    }

    public static Location getHome(UUID uuid, String name) {
        return homes.getOrDefault(uuid, new HashMap<>()).get(name);
    }

    public static boolean renameHome(UUID uuid, String oldName, String newName) {
        if (!homes.containsKey(uuid)) return false;
        Map<String, Location> userHomes = homes.get(uuid);

        if (!userHomes.containsKey(oldName) || userHomes.containsKey(newName)) return false;

        Location loc = userHomes.remove(oldName);
        userHomes.put(newName, loc);

        config.set(uuid.toString() + "." + oldName, null);
        config.set(uuid.toString() + "." + newName, loc);
        saveHomes();

        return true;
    }

    public static boolean deleteHome(UUID uuid, String name) {
        if (homes.containsKey(uuid) && homes.get(uuid).containsKey(name)) {
            homes.get(uuid).remove(name);
            config.set(uuid.toString() + "." + name, null);
            saveHomes();
            return true;
        }
        return false;
    }

    public static boolean homeExists(UUID uuid, String name) {
        return homes.containsKey(uuid) && homes.get(uuid).containsKey(name);
    }

    public static Map<String, Location> getHomes(UUID uuid) {
        return homes.getOrDefault(uuid, new HashMap<>());
    }
}
