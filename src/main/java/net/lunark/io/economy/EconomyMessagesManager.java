package net.lunark.io.economy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import net.lunark.io.ServerEssentials;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EconomyMessagesManager {

    private static File file;
    private static FileConfiguration config;
    private static final Map<String, String> messages = new HashMap<>();
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static ServerEssentials plugin;


    public static void setup(ServerEssentials pl) {
        plugin = pl;

        File folder = new File(plugin.getDataFolder(), "messages");
        if (!folder.exists()) folder.mkdirs();

        file = new File(folder, "economy.yml");
        if (!file.exists()) {
            plugin.saveResource("messages/economy.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(file);
        loadMessages();
    }


    private static void loadMessages() {
        messages.clear();
        for (String key : config.getKeys(true)) {
            if (config.isString(key)) {
                messages.put(key, config.getString(key));
            }
        }
    }


    public static void reload() {
        if (plugin == null) {
            Bukkit.getLogger().severe("[ServerEssentials] EconomyMessagesManager not initialized! Call setup(plugin) first.");
            return;
        }

        if (!file.exists()) {
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
            plugin.saveResource("messages/economy.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(file);
        loadMessages();

        Bukkit.getLogger().info("[ServerEssentials] EconomyMessagesManager reloaded successfully.");
    }


    public static Component getMessage(String key, Map<String, String> placeholders) {
        String raw = messages.getOrDefault(key, "<red>Missing message: " + key + "</red>");

        if (placeholders != null && !placeholders.isEmpty()) {
            String formatted = raw;

            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                String value = entry.getValue();

                formatted = formatted
                        .replace("{" + entry.getKey() + "}", value)
                        .replace("%" + entry.getKey() + "%", value);
            }

            raw = formatted;
        }

        return miniMessage.deserialize(raw);
    }


    public static Component getMessage(String key) {
        return getMessage(key, null);
    }


    public static void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
