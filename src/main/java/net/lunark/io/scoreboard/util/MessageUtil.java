package net.lunark.io.scoreboard.util;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class MessageUtil {
    private final JavaPlugin plugin;
    private final String path;
    private File file;
    private YamlConfiguration config;

    public MessageUtil(JavaPlugin plugin, String path) {
        this.plugin = plugin;
        this.path = path;
        reload();
    }

    public void reload() {
        file = new File(plugin.getDataFolder(), path);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource(path, false); // copy default from jar
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void send(Player player, String path, String... replacements) {
        String msg = config.getString(path, "<red>Missing message: " + path);
        for (int i = 0; i < replacements.length; i += 2) {
            msg = msg.replace(replacements[i], replacements[i + 1]);
        }
        player.sendMessage(MiniMessage.miniMessage().deserialize(msg));
    }
}
