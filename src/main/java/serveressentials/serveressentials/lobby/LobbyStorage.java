package serveressentials.serveressentials.lobby;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import serveressentials.serveressentials.ServerEssentials;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LobbyStorage {

    private static File file;
    private static FileConfiguration config;
    private static Map<String, Location> worldLobbies = new HashMap<>();
    private static Location globalLobby;

    public static void setup() {
        File storageFolder = new File(ServerEssentials.getInstance().getDataFolder(), "storage");
        if (!storageFolder.exists()) storageFolder.mkdirs();

        file = new File(storageFolder, "lobby.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(file);

        globalLobby = config.getLocation("global");

        if (config.isConfigurationSection("worlds")) {
            for (String world : config.getConfigurationSection("worlds").getKeys(false)) {
                Location loc = config.getLocation("worlds." + world);
                if (loc != null) worldLobbies.put(world, loc);
            }
        }
    }

    public static void setLobby(Location location) {
        globalLobby = location;
        config.set("global", location);
        save();
    }

    public static void setWorldLobby(String world, Location location) {
        worldLobbies.put(world, location);
        config.set("worlds." + world, location);
        save();
    }

    public static Location getLobby(String world) {
        if (LobbyConfig.isPerWorld()) {
            return worldLobbies.getOrDefault(world, globalLobby);
        } else {
            return globalLobby;
        }
    }

    public static void removeLobby(String world) {
        if (world == null) {
            globalLobby = null;
            config.set("global", null);
        } else {
            worldLobbies.remove(world);
            config.set("worlds." + world, null);
        }
        save();
    }

    public static boolean hasLobby(String world) {
        return getLobby(world) != null;
    }

    private static void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
