package net.lunark.io.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class ServerMessages {

    private static ServerMessages instance; // optional global reference

    private final Plugin plugin;
    private final File file;
    private FileConfiguration config;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ServerMessages(Plugin plugin, String path) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), path);

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource(path, false);
        }

        this.config = YamlConfiguration.loadConfiguration(file);
        instance = this;
    }

    /**
     * Reload the file from disk
     */
    public void reload() {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource(file.getName(), false);
        }

        config = YamlConfiguration.loadConfiguration(file);
        plugin.getLogger().info("[ServerMessages] Reloaded " + file.getName() + " successfully.");
    }

    /**
     * Static helper to reload if instance is available
     */
    public static void fullReload() {
        if (instance != null) {
            instance.reload();
        }
    }

    public void addDefault(String path, Object value) {
        if (!config.contains(path)) {
            config.set(path, value);
            save();
        }
    }

    public Component get(String path) {
        String raw = config.getString(path, "<red>Missing message: " + path);
        return miniMessage.deserialize(raw);
    }

    public Component get(String path, String... placeholders) {
        String raw = config.getString(path, "<red>Missing message: " + path);

        if (placeholders != null && placeholders.length % 2 == 0) {
            for (int i = 0; i < placeholders.length; i += 2) {
                raw = raw.replace(placeholders[i], placeholders[i + 1]);
            }
        }

        return miniMessage.deserialize(raw);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save " + file.getName() + "!");
            e.printStackTrace();
        }
    }

    /**
     * Static accessor for global instance (optional)
     */
    public static ServerMessages getInstance() {
        return instance;
    }
}
