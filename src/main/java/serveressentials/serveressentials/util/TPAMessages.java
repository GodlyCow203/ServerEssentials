package serveressentials.serveressentials.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import serveressentials.serveressentials.ServerEssentials;

import java.io.File;
import java.io.IOException;

public class TPAMessages {

    private final ServerEssentials plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private FileConfiguration config;
    private File file;

    public TPAMessages(ServerEssentials plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        file = new File(plugin.getDataFolder(), "messages/tpa.yml");

        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        if (!file.exists()) plugin.saveResource("messages/tpa.yml", false);

        config = YamlConfiguration.loadConfiguration(file);
    }

    public void reload() {
        load();
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Component get(String path, String... placeholders) {
        String msg = config.getString(path, "<red>Missing message for " + path);

        if (placeholders != null && placeholders.length % 2 == 0) {
            for (int i = 0; i < placeholders.length; i += 2) {
                msg = msg.replace(placeholders[i], placeholders[i + 1]);
            }
        }

        return miniMessage.deserialize(msg);
    }
}
