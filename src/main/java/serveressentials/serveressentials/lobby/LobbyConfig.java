package serveressentials.serveressentials.lobby;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import serveressentials.serveressentials.ServerEssentials;

import java.io.File;
import java.io.IOException;

public class LobbyConfig {

    private static File file;
    private static FileConfiguration config;

    public static void setup() {
        File configFolder = new File(ServerEssentials.getInstance().getDataFolder(), "config/lobby");
        if (!configFolder.exists()) configFolder.mkdirs();

        file = new File(configFolder, "lobby.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
                config = YamlConfiguration.loadConfiguration(file);
                config.set("cooldown", 5);
                config.set("animation", true);
                config.set("perWorld", false);
                config.set("teleportOnJoin", false);
                config.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    public static int getCooldown() {
        return config.getInt("cooldown", 5);
    }

    public static boolean isAnimationEnabled() {
        return config.getBoolean("animation", true);
    }

    public static boolean isPerWorld() {
        return config.getBoolean("perWorld", false);
    }

    public static boolean isTeleportOnJoin() {
        return config.getBoolean("teleportOnJoin", false);
    }

    public static void reload() {
        config = YamlConfiguration.loadConfiguration(file);
    }
}
