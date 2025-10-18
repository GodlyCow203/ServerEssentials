package serveressentials.serveressentials.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import serveressentials.serveressentials.ServerEssentials;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class PlayerMessages {

    private final ServerEssentials plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private FileConfiguration config;
    private File file;

    // Static registry for all PlayerMessages instances
    private static final Set<PlayerMessages> INSTANCES = new HashSet<>();

    public PlayerMessages(ServerEssentials plugin) {
        this.plugin = plugin;
        load();

        // Register this instance
        INSTANCES.add(this);
    }

    /**
     * Load or create player.yml
     */
    public void load() {
        file = new File(plugin.getDataFolder(), "messages/player.yml");

        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        if (!file.exists()) plugin.saveResource("messages/player.yml", false);

        config = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Reload the file from disk
     */
    public void reload() {
        if (file != null && file.exists()) {
            config = YamlConfiguration.loadConfiguration(file);
        } else {
            load(); // recreate if missing
        }
    }

    /**
     * Reload all registered PlayerMessages instances
     */
    public static void reloadAll() {
        for (PlayerMessages messages : INSTANCES) {
            messages.reload();
        }
    }

    /**
     * Save changes to disk
     */
    public void save() {
        try {
            if (file != null) config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add a default message if missing
     */
    public void addDefault(String path, String defaultMessage) {
        if (!config.contains(path)) {
            config.set(path, defaultMessage);
            save();
        }
    }

    /**
     * Get a message as a MiniMessage Component with optional placeholders
     * Example placeholders: "{player}", "{world}"
     */
    public Component get(String path, String... placeholders) {
        String msg = config.getString(path, "<red>Missing message for " + path);

        if (placeholders != null && placeholders.length % 2 == 0) {
            for (int i = 0; i < placeholders.length; i += 2) {
                msg = msg.replace(placeholders[i], placeholders[i + 1]);
            }
        }

        return miniMessage.deserialize(msg);
    }

    /**
     * Get the raw configuration
     */
    public FileConfiguration getConfig() {
        return config;
    }
}
