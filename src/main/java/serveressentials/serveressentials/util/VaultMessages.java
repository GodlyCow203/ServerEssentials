package serveressentials.serveressentials.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import serveressentials.serveressentials.ServerEssentials;

import java.io.File;
import java.io.IOException;

public class VaultMessages {

    private static VaultMessages instance;

    private final ServerEssentials plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private FileConfiguration config;
    private File file;

    public VaultMessages(ServerEssentials plugin) {
        this.plugin = plugin;
        instance = this;
        load();
    }

    public static VaultMessages getInstance() {
        return instance;
    }

    private void load() {
        file = new File(plugin.getDataFolder(), "messages/vault.yml");

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        if (!file.exists()) {
            plugin.saveResource("messages/vault.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    public void reload() {
        load();
        plugin.getLogger().info("[VaultMessages] Reloaded messages/vault.yml successfully.");
    }

    public static void fullReload() {
        if (instance != null) {
            instance.reload();
        }
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save messages/vault.yml!");
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
        String msg = config.getString(path, "<red>Missing message for " + path);

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
