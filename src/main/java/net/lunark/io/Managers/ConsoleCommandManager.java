package net.lunark.io.Managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ConsoleCommandManager {

    private static JavaPlugin plugin;
    private static FileConfiguration config;
    private static File configFile;

    public ConsoleCommandManager(JavaPlugin pl) {
        plugin = pl;
        loadConfig();
    }

    private static void loadConfig() {
        File pluginFolder = plugin.getDataFolder();
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs();
        }

        File configFolder = new File(pluginFolder, "config/ConsoleCommands");
        if (!configFolder.exists()) {
            configFolder.mkdirs();
        }

        configFile = new File(configFolder, "consolecommands.yml");

        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(configFile);
                defaultConfig.set("example.console", List.of("say %player% executed a command!"));
                defaultConfig.set("example.player", List.of("give %player% minecraft:stone 1"));
                defaultConfig.save(configFile);
            } catch (IOException e) {
                Bukkit.getLogger().severe("[ServerEssentials] Could not create consolecommands.yml!");
                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public static void reload() {
        if (plugin == null) {
            Bukkit.getLogger().severe("[ServerEssentials] ConsoleCommandManager plugin reference is null!");
            return;
        }

        if (configFile == null || !configFile.exists()) {
            loadConfig();
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        Bukkit.getLogger().info("[ServerEssentials] ConsoleCommandManager reloaded.");
    }

    public void runCommands(String path, Player player) {
        if (config.contains(path + ".console")) {
            List<String> consoleCmds = config.getStringList(path + ".console");
            for (String cmd : consoleCmds) {
                if (cmd == null || cmd.trim().isEmpty()) continue;
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
            }
        }

        if (config.contains(path + ".player")) {
            List<String> playerCmds = config.getStringList(path + ".player");
            for (String cmd : playerCmds) {
                if (cmd == null || cmd.trim().isEmpty()) continue;
                player.performCommand(cmd.replace("%player%", player.getName()));
            }
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }
}
