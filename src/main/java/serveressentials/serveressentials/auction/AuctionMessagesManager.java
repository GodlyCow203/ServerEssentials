package serveressentials.serveressentials.auction;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import serveressentials.serveressentials.ServerEssentials;

import java.io.File;
import java.io.IOException;

public class AuctionMessagesManager {

    private static AuctionMessagesManager instance;
    private final ServerEssentials plugin;
    private File messagesFile;
    private FileConfiguration messagesConfig;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public AuctionMessagesManager(ServerEssentials plugin) {
        this.plugin = plugin;
        instance = this;
        loadMessages();
    }

    public static AuctionMessagesManager getInstance() {
        return instance;
    }

    /**
     * Reload messages from disk, reloading all changes in messages/auction.yml
     */
    public void reload() {
        if (messagesFile == null) {
            loadMessages();
            return;
        }

        if (!messagesFile.exists()) {
            plugin.saveResource("messages/auction.yml", false);
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        Bukkit.getLogger().info("[ServerEssentials] AuctionMessagesManager reloaded successfully.");
    }

    /**
     * Global static reload shortcut.
     */
    public static void fullReload() {
        if (instance == null) {
            Bukkit.getLogger().warning("[ServerEssentials] Tried to reload AuctionMessagesManager before initialization!");
            return;
        }
        instance.reload();
    }

    public void loadMessages() {
        messagesFile = new File(plugin.getDataFolder(), "messages/auction.yml");

        if (!messagesFile.getParentFile().exists()) messagesFile.getParentFile().mkdirs();
        if (!messagesFile.exists()) plugin.saveResource("messages/auction.yml", false);

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public Component getMessage(String path, String... placeholders) {
        String msg = messagesConfig.getString(path, "<red>Missing message for " + path);

        if (placeholders != null && placeholders.length % 2 == 0) {
            for (int i = 0; i < placeholders.length; i += 2) {
                msg = msg.replace(placeholders[i], placeholders[i + 1]);
            }
        }

        return miniMessage.deserialize(msg);
    }

    public void addDefault(String path, String defaultMessage) {
        if (!messagesConfig.contains(path)) {
            messagesConfig.set(path, defaultMessage);
            save();
        }
    }

    public void save() {
        try {
            messagesConfig.save(messagesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfig() {
        return messagesConfig;
    }
}
