package serveressentials.serveressentials.Managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class JoinLeaveManager {
    private static File configFile;
    private static FileConfiguration config;

    public static void load() {
        // Plugin folder: plugins/ServerEssentials/
        File pluginFolder = Bukkit.getPluginManager().getPlugin("ServerEssentials").getDataFolder();
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs();
        }

        // Folder: plugins/ServerEssentials/config/Join_Leave_Messages/
        File configFolder = new File(pluginFolder, "config/Join_Leave_Messages");
        if (!configFolder.exists()) {
            configFolder.mkdirs();
        }

        // File: plugins/ServerEssentials/config/Join_Leave_Messages/joinleave.yml
        configFile = new File(configFolder, "joinleave.yml");

        if (!configFile.exists()) {
            saveDefaultConfig();
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    private static void saveDefaultConfig() {
        try {
            configFile.createNewFile();
            FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(configFile);
            defaultConfig.set("join-message", "&8[&e+&8] &e%player%");
            defaultConfig.set("leave-message", "&8[&6-&8] &6%player%");
            defaultConfig.save(configFile);
        } catch (IOException e) {
            Bukkit.getLogger().severe("[ServerEssentials] Could not create joinleave.yml!");
            e.printStackTrace();
        }
    }

    public static FileConfiguration getConfig() {
        return config;
    }

    public static void reload() {
        if (configFile == null) {
            load(); // ✅ If not loaded yet, load it
            return;
        }

        // ✅ Reload the YAML file
        config = YamlConfiguration.loadConfiguration(configFile);

        // ✅ If the file was deleted manually, recreate it
        if (!configFile.exists()) {
            saveDefaultConfig();
            config = YamlConfiguration.loadConfiguration(configFile);
        }

        Bukkit.getLogger().info("[ServerEssentials] JoinLeaveManager reloaded successfully.");
    }
}
