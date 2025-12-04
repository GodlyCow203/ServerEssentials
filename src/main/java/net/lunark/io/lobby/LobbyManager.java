package net.lunark.io.lobby;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import net.lunark.io.ServerEssentials;

import java.io.File;
import java.io.IOException;

public class LobbyManager {

    private static File file;
    private static FileConfiguration config;
    private static Location lobby;


    public static void setup() {
        File dataFolder = ServerEssentials.getInstance().getDataFolder();
        File storageFolder = new File(dataFolder, "storage");

        if (!storageFolder.exists() && storageFolder.mkdirs()) {
            Bukkit.getLogger().info("[ServerEssentials] Created /storage folder.");
        }

        file = new File(storageFolder, "lobby.yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
                Bukkit.getLogger().info("[ServerEssentials] Created lobby.yml.");
            } catch (IOException e) {
                Bukkit.getLogger().severe("[ServerEssentials] Failed to create lobby.yml!");
                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
        loadLobbyFromConfig();
    }


    public static void reload() {
        if (file == null) {
            setup();
            return;
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
                Bukkit.getLogger().info("[ServerEssentials] Recreated missing lobby.yml.");
            } catch (IOException e) {
                Bukkit.getLogger().severe("[ServerEssentials] Failed to recreate lobby.yml!");
                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
        loadLobbyFromConfig();

        Bukkit.getLogger().info("[ServerEssentials] LobbyManager reloaded successfully.");
    }


    public static void fullReload() {
        reload();

        if (lobby != null) {
            Bukkit.getLogger().info("[ServerEssentials] Lobby loaded at: " +
                    String.format("(%.1f, %.1f, %.1f) in world '%s'",
                            lobby.getX(), lobby.getY(), lobby.getZ(),
                            lobby.getWorld() != null ? lobby.getWorld().getName() : "null"));
        } else {
            Bukkit.getLogger().warning("[ServerEssentials] No lobby location is currently set!");
        }
    }


    private static void loadLobbyFromConfig() {
        if (!config.contains("lobby")) {
            lobby = null;
            return;
        }

        World world = Bukkit.getWorld(config.getString("lobby.world"));
        if (world == null) {
            lobby = config.getLocation("lobby");
        } else {
            double x = config.getDouble("lobby.x");
            double y = config.getDouble("lobby.y");
            double z = config.getDouble("lobby.z");
            float yaw = (float) config.getDouble("lobby.yaw", 0.0);
            float pitch = (float) config.getDouble("lobby.pitch", 0.0);
            lobby = new Location(world, x, y, z, yaw, pitch);
        }
    }


    public static void setLobby(Location location) {
        lobby = location;
        config.set("lobby.world", location.getWorld().getName());
        config.set("lobby.x", location.getX());
        config.set("lobby.y", location.getY());
        config.set("lobby.z", location.getZ());
        config.set("lobby.yaw", location.getYaw());
        config.set("lobby.pitch", location.getPitch());
        save();
    }


    public static Location getLobby() {
        if (lobby == null) {
            loadLobbyFromConfig();
        }
        return lobby;
    }

    public static void removeLobby() {
        lobby = null;
        config.set("lobby", null);
        save();
    }


    public static boolean hasLobby() {
        return getLobby() != null;
    }


    private static void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().severe("[ServerEssentials] Failed to save lobby.yml!");
            e.printStackTrace();
        }
    }
}
