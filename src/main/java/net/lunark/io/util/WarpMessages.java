package net.lunark.io.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import net.lunark.io.ServerEssentials;

import java.io.File;
import java.io.IOException;

public class WarpMessages {

    private static WarpMessages instance;

    private final ServerEssentials plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private FileConfiguration config;
    private File file;

    public WarpMessages(ServerEssentials plugin) {
        this.plugin = plugin;
        instance = this;
        load();
    }

    public static WarpMessages getInstance() {
        return instance;
    }

    private void load() {
        file = new File(plugin.getDataFolder(), "messages/warp.yml");

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        if (!file.exists()) {
            plugin.saveResource("messages/warp.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    public void reload() {
        load();
        plugin.getLogger().info("[WarpMessages] Reloaded messages/warp.yml successfully.");
    }

    public static void fullReload() {
        if (instance != null) {
            instance.reload();
        }
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save messages/warp.yml!");
            e.printStackTrace();
        }
    }

    public void addDefault(String path, String defaultMessage) {
        if (!config.contains(path)) {
            config.set(path, defaultMessage);
            save();
        }
    }

    public Component get(String path, String... placeholders) {
        String msg = config.getString(path, "<red>Missing message for " + path + "</red>");

        if (placeholders != null && placeholders.length % 2 == 0) {
            for (int i = 0; i < placeholders.length; i += 2) {
                msg = msg.replace(placeholders[i], placeholders[i + 1]);
            }
        }

        return miniMessage.deserialize(msg);
    }

    public FileConfiguration getConfig() {
        return config;
    }
}
