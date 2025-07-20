package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class LobbyManager {
    private static File file;
    private static FileConfiguration config;
    private static Location lobby;

    public static void setup() {
        file = new File(Bukkit.getServer().getPluginManager().getPlugin("ServerEssentials").getDataFolder(), "lobby.yml");

        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
        lobby = config.getLocation("lobby");
    }

    public static void loadLobby() {
        if (file == null || config == null) setup(); // in case not setup yet
        lobby = config.getLocation("lobby");
    }

    public static void setLobby(Location location) {
        lobby = location;
        config.set("lobby", location);
        save();
    }

    public static Location getLobby() {
        return lobby;
    }

    public static void removeLobby() {
        lobby = null;
        config.set("lobby", null);
        save();
    }

    public static boolean hasLobby() {
        return lobby != null;
    }

    private static void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
