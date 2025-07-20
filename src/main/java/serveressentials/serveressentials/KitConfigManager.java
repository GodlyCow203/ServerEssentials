package serveressentials.serveressentials;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class KitConfigManager {

    private static FileConfiguration config;
    private static File file;

    public static void setup(JavaPlugin plugin) {
        file = new File(plugin.getDataFolder(), "kits.yml");

        if (!file.exists()) {
            plugin.saveResource("kits.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(file);

        try (InputStream defStream = plugin.getResource("kits.yml")) {
            if (defStream != null) {
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defStream, StandardCharsets.UTF_8));
                config.setDefaults(defConfig);
                config.options().copyDefaults(true);
                save();
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to apply default kit config:");
            e.printStackTrace();
        }
    }

    public static FileConfiguration getConfig() {
        return config;
    }

    public static void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void reload() {
        config = YamlConfiguration.loadConfiguration(file);
    }
}
