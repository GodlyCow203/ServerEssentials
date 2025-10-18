package serveressentials.serveressentials.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import serveressentials.serveressentials.ServerEssentials;

import java.io.File;
import java.io.IOException;

public class RankMessages {

    private final ServerEssentials plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private File file;
    private FileConfiguration config;

    public RankMessages(ServerEssentials plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        file = new File(plugin.getDataFolder(), "messages/ranks.yml");
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        if (!file.exists()) plugin.saveResource("messages/ranks.yml", false);

        config = YamlConfiguration.loadConfiguration(file);
    }

    public void reload() {
        load();
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save ranks.yml!");
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
        String msg = config.getString(path, "<red>Missing message: " + path);

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
