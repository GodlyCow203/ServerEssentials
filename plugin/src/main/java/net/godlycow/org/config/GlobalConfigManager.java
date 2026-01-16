package net.godlycow.org.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class GlobalConfigManager {

    private final Plugin plugin;
    private FileConfiguration cfg;
    private final File configFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public GlobalConfigManager(Plugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        load();
    }

    public void load() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        this.cfg = YamlConfiguration.loadConfiguration(configFile);
    }

    public void reload() {
        load();
    }

    public FileConfiguration getConfig() {
        return cfg;
    }

    public String getString(String path, String def) {
        return cfg.getString(path, def);
    }

    public int getInt(String path, int def) {
        return cfg.getInt(path, def);
    }

    public boolean getBoolean(String path, boolean def) {
        return cfg.getBoolean(path, def);
    }

    public void set(String path, Object value) {
        cfg.set(path, value);
        try {
            cfg.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save config.yml", e);
        }
    }
}
