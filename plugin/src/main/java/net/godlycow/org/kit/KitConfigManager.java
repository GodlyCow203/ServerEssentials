package net.godlycow.org.kit;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import net.godlycow.org.EssC;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.io.File;

public class KitConfigManager {

    private static FileConfiguration config;
    private static File file;
    private static final String FILE_PATH = "kits/kits.yml";

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
            EssC.getInstance().getLogger().severe("Could not save kits.yml: " + e.getMessage());
        }
    }

    public static void reload() {
        if (file == null) {
            file = new File(EssC.getInstance().getDataFolder(), FILE_PATH);
        }

        if (!file.exists()) {
            EssC.getInstance().saveResource(FILE_PATH, false);
        }

        config = YamlConfiguration.loadConfiguration(file);
        KitManager.loadKits(config);
    }
}