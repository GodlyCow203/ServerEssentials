package serveressentials.serveressentials.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import serveressentials.serveressentials.ServerEssentials;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RTPMessages {

    private static RTPMessages instance;
    private final ServerEssentials plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private File file;
    private FileConfiguration config;

    public RTPMessages(ServerEssentials plugin) {
        this.plugin = plugin;
        instance = this;
        load();
    }

    public static RTPMessages getInstance() {
        return instance;
    }

    private void load() {
        file = new File(plugin.getDataFolder(), "messages/rtp.yml");
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        if (!file.exists()) plugin.saveResource("messages/rtp.yml", false);
        config = YamlConfiguration.loadConfiguration(file);
    }

    public static void fullReload() {
        if (instance == null) return;
        instance.load();
        instance.plugin.getLogger().info("[RTPMessages] Reloaded messages/rtp.yml successfully.");
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save rtp.yml!");
            e.printStackTrace();
        }
    }

    public List<Component> getList(String path, String... placeholders) {
        List<String> rawList = config.getStringList(path);
        List<Component> components = new ArrayList<>();

        for (String line : rawList) {
            if (placeholders != null && placeholders.length % 2 == 0) {
                for (int i = 0; i < placeholders.length; i += 2) {
                    line = line.replace(placeholders[i], placeholders[i + 1]);
                }
            }
            components.add(miniMessage.deserialize(line));
        }
        return components;
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
