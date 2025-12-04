package net.lunark.io.kit;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import net.lunark.io.ServerEssentials;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class KitConfigManager {

    private static FileConfiguration config;
    private static File file;
    private static final String FILE_PATH = "kits/kits.yml"; // Moved to subfolder

    public static void setup(JavaPlugin plugin) {
        File folder = new File(plugin.getDataFolder(), "kits");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        file = new File(plugin.getDataFolder(), FILE_PATH);

        if (!file.exists()) {
            plugin.saveResource(FILE_PATH, false);
        }

        config = YamlConfiguration.loadConfiguration(file);

        // Apply defaults if needed
        try (InputStream defStream = plugin.getResource(FILE_PATH)) {
            if (defStream != null) {
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(defStream, StandardCharsets.UTF_8));
                config.setDefaults(defConfig);
                config.options().copyDefaults(true);
                save();
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to apply default kit config: " + e.getMessage());
        }
    }

    public static FileConfiguration getConfig() {
        return config;
    }

    public static void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            ServerEssentials.getInstance().getLogger().severe("Could not save kits.yml: " + e.getMessage());
        }
    }

    public static void reload() {
        if (file == null) {
            file = new File(ServerEssentials.getInstance().getDataFolder(), FILE_PATH);
        }

        if (!file.exists()) {
            ServerEssentials.getInstance().saveResource(FILE_PATH, false);
        }

        config = YamlConfiguration.loadConfiguration(file);
        // Reload kits into manager
        KitManager.loadKits(config);
    }
}