package net.lunark.io.util;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class FunMessages {
    private final Plugin plugin;
    private final File file;
    private FileConfiguration config;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    // Registry of all FunMessages instances
    private static final Set<FunMessages> INSTANCES = new HashSet<>();

    public FunMessages(Plugin plugin, String path) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), path);

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource(path, false);
        }

        this.config = YamlConfiguration.loadConfiguration(file);

        // Add to registry
        INSTANCES.add(this);
    }

    public void reload() {
        if (!file.exists()) {
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
            plugin.saveResource(file.getName(), false);
        }

        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().severe("Could not reload " + file.getName() + "!");
            e.printStackTrace();
        }
    }

    // Reload all registered FunMessages
    public static void reloadAll() {
        for (FunMessages messages : INSTANCES) {
            messages.reload();
        }
    }

    public Component get(String path, String... placeholders) {
        String msg = getConfig().getString(path, "<red>Missing message: " + path);

        if (placeholders != null && placeholders.length % 2 == 0) {
            for (int i = 0; i < placeholders.length; i += 2) {
                msg = msg.replace(placeholders[i], placeholders[i + 1]);
            }
        }

        return miniMessage.deserialize(msg);
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
}
