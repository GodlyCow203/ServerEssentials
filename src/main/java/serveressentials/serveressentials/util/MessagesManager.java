package serveressentials.serveressentials.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import serveressentials.serveressentials.ServerEssentials;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MessagesManager {

    private static MessagesManager instance;

    private final ServerEssentials plugin;
    private final Map<String, FileConfiguration> loadedFiles = new HashMap<>();
    private FileConfiguration messagesConfig; // for backward compatibility
    private File messagesFile; // for backward compatibility

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public MessagesManager(ServerEssentials plugin) {
        this.plugin = plugin;
        instance = this;
        loadMessages(); // loads default staff.yml for backward compatibility
    }

    public static MessagesManager getInstance() {
        return instance;
    }

    // ---------------------- Existing single file methods ----------------------
    private void loadMessages() {
        messagesFile = new File(plugin.getDataFolder(), "messages/staff.yml");

        if (!messagesFile.getParentFile().exists()) {
            messagesFile.getParentFile().mkdirs();
        }

        if (!messagesFile.exists()) {
            plugin.saveResource("messages/staff.yml", false);
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        loadedFiles.put("staff.yml", messagesConfig);
    }

    public void addDefault(String path, String defaultMessage) {
        if (!messagesConfig.contains(path)) {
            messagesConfig.set(path, defaultMessage);
        }
    }

    public Component getMessageComponent(String path, String... placeholders) {
        return get("staff.yml", path, placeholders);
    }

    public void save() {
        save("staff.yml");
    }

    public FileConfiguration getConfig() {
        return messagesConfig;
    }

    // ---------------------- New multi-file support ----------------------

    /**
     * Load or reload any messages file dynamically
     */
    public void load(String fileName) {
        File file = new File(plugin.getDataFolder(), "messages/" + fileName);

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        if (!file.exists()) {
            plugin.saveResource("messages/" + fileName, false);
        }

        loadedFiles.put(fileName, YamlConfiguration.loadConfiguration(file));
    }

    /**
     * Reload a specific messages file
     */
    public void reload(String fileName) {
        load(fileName);
        plugin.getLogger().info("[MessagesManager] Reloaded messages/" + fileName + " successfully.");
    }

    /**
     * Reload all previously loaded files
     */
    public void reloadAll() {
        for (String fileName : loadedFiles.keySet()) {
            load(fileName);
        }
        plugin.getLogger().info("[MessagesManager] Reloaded all message files (" + loadedFiles.size() + ").");
    }

    /**
     * Static helper to reload all files if instance exists
     */
    public static void fullReload() {
        if (instance != null) {
            instance.reloadAll();
        }
    }

    /**
     * Get a message from a specific file with optional placeholders
     */
    public Component get(String fileName, String path, String... placeholders) {
        FileConfiguration config = loadedFiles.get(fileName);
        if (config == null) {
            load(fileName);
            config = loadedFiles.get(fileName);
        }

        String msg = config.getString(path, "<red>Missing message for " + path);

        if (placeholders != null && placeholders.length % 2 == 0) {
            for (int i = 0; i < placeholders.length; i += 2) {
                msg = msg.replace(placeholders[i], placeholders[i + 1]);
            }
        }

        return miniMessage.deserialize(msg);
    }

    /**
     * Save a specific messages file
     */
    public void save(String fileName) {
        FileConfiguration config = loadedFiles.get(fileName);
        if (config == null) return;

        File file = new File(plugin.getDataFolder(), "messages/" + fileName);
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save messages/" + fileName + "!");
            e.printStackTrace();
        }
    }

    /**
     * Get raw config of a specific file
     */
    public FileConfiguration getConfig(String fileName) {
        return loadedFiles.get(fileName);
    }
}
