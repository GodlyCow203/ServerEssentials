package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class JoinLeaveManager {
    private static File configFile;
    private static FileConfiguration config;

    public static void load() {
        configFile = new File(Bukkit.getPluginManager().getPlugin("ServerEssentials").getDataFolder(), "joinleave.yml");

        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            saveDefaultConfig();
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    private static void saveDefaultConfig() {
        try {
            configFile.createNewFile();
            FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(configFile);
            defaultConfig.set("join-message", "&7[&9+&7] %player% ");
            defaultConfig.set("leave-message", "&7[&c-&7] %player% ");
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
        config = YamlConfiguration.loadConfiguration(configFile);
    }
}
