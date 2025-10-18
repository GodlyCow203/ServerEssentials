package serveressentials.serveressentials.util;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class HomeMessages {

    private final Plugin plugin;
    private final File file;
    private FileConfiguration config;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    // Registry for static reload
    private static final Set<HomeMessages> INSTANCES = new HashSet<>();

    public HomeMessages(Plugin plugin, String path) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), path);

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource(path, false);
        }

        this.config = YamlConfiguration.loadConfiguration(file);

        // Add some sensible defaults if missing
        addDefault("gui.title.main", "<white><bold>Homes</bold></white>");
        addDefault("gui.title.confirm", "<white><bold>Confirm</bold></white>");
        addDefault("msg.no-permission", "<red>You don't have permission to do that.");
        addDefault("msg.home-set", "<green>Home <gold>{home}</gold> set at <yellow>{x}, {y}, {z}</yellow>.");
        addDefault("msg.home-removed", "<green>Home <gold>{home}</gold> removed.");
        addDefault("msg.rename-prompt", "<yellow>Type the new name for your home in chat.");
        addDefault("msg.renamed", "<green>Home renamed to <gold>{name}</gold>.");
        addDefault("msg.teleport", "<green>Teleporting to <gold>{home}</gold>...");
        addDefault("lore.empty", "<gray>Empty");
        addDefault("lore.click.set", "<green>Click to set this home.");
        addDefault("lore.click.remove", "<red>Click to remove this home.");
        addDefault("lore.click.rename", "<aqua>Click to rename this home.");
        save();

        // Add this instance to the registry
        INSTANCES.add(this);
    }

    /**
     * Reload this instance from disk
     */
    public void reload() {
        if (!file.exists()) {
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
            plugin.saveResource(file.getName(), false);
        }

        try {
            config.load(file);
        } catch (IOException | org.bukkit.configuration.InvalidConfigurationException e) {
            plugin.getLogger().severe("Could not reload " + file.getName() + "!");
            e.printStackTrace();
        }
    }

    /**
     * Reload all HomeMessages instances
     */
    public static void reloadAll() {
        for (HomeMessages messages : INSTANCES) {
            messages.reload();
        }
    }

    public void addDefault(String path, Object value) {
        if (!config.contains(path)) {
            config.set(path, value);
            save();
        }
    }

    public Component get(String path) {
        String raw = config.getString(path, "<red>Missing message: " + path);
        return miniMessage.deserialize(raw);
    }

    public Component get(String path, String... placeholders) {
        String raw = config.getString(path, "<red>Missing message: " + path);

        if (placeholders != null && placeholders.length % 2 == 0) {
            for (int i = 0; i < placeholders.length; i += 2) {
                raw = raw.replace(placeholders[i], placeholders[i + 1]);
            }
        }

        return miniMessage.deserialize(raw);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save " + file.getName() + "!");
            e.printStackTrace();
        }
    }
}
