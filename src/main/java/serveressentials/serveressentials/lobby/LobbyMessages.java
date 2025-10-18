package serveressentials.serveressentials.lobby;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import serveressentials.serveressentials.ServerEssentials;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LobbyMessages {

    private static File file;
    private static FileConfiguration config;
    private static final Map<String, String> defaults = new HashMap<>();

    static {
        defaults.put("prefix", "<gradient:#ff7f50:#ff1493>[Lobby]</gradient> ");
        defaults.put("no-lobby", "<red>No lobby is set.");
        defaults.put("teleport-lobby", "<green>Teleported to lobby.");
        defaults.put("set-lobby", "<green>Lobby location set!");
        defaults.put("removed-lobby", "<yellow>Lobby location removed.");
        defaults.put("only-players", "<red>Only players can use this command.");
        defaults.put("no-permission", "<red>You do not have permission to do this.");
        defaults.put("world-set", "<green>Lobby for world <world> set!");
        defaults.put("cooldown-active", "<red>Please wait <time> seconds before teleporting again.");
    }

    /** Initial setup and loading */
    public static void setup() {
        File messagesFolder = new File(ServerEssentials.getInstance().getDataFolder(), "messages");
        if (!messagesFolder.exists()) messagesFolder.mkdirs();

        file = new File(messagesFolder, "lobby.yml");

        boolean saveNeeded = false;

        if (!file.exists()) {
            try {
                file.createNewFile();
                saveNeeded = true;
            } catch (IOException e) {
                Bukkit.getLogger().severe("[ServerEssentials] Failed to create lobby.yml messages file!");
                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(file);

        for (Map.Entry<String, String> entry : defaults.entrySet()) {
            if (!config.contains(entry.getKey())) {
                config.set(entry.getKey(), entry.getValue());
                saveNeeded = true;
            }
        }

        if (saveNeeded) saveFile();
    }

    /** Reload the lobby.yml file from disk */
    public static void reload() {
        if (file == null || !file.exists()) {
            setup();
        } else {
            config = YamlConfiguration.loadConfiguration(file);
        }
    }

    /** Save the file */
    public static void saveFile() {
        if (file == null || config == null) return;

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Get a message by key */
    public static Component get(String key) {
        if (config == null) setup();
        String raw = config.getString(key, defaults.getOrDefault(key, "<red>Missing message: " + key));
        return MiniMessage.miniMessage().deserialize(raw);
    }

    /** Get a message with placeholders */
    public static Component getWithPlaceholders(String key, Map<String, String> placeholders) {
        if (config == null) setup();
        String raw = config.getString(key, defaults.getOrDefault(key, "<red>Missing message: " + key));

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            raw = raw.replace("%" + entry.getKey() + "%", entry.getValue());
            raw = raw.replace("<" + entry.getKey() + ">", entry.getValue());

        }

        return MiniMessage.miniMessage().deserialize(raw);
    }
}
