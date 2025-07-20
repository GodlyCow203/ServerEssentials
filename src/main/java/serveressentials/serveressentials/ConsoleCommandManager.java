package serveressentials.serveressentials;


import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

public class ConsoleCommandManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;
    private File configFile;

    public ConsoleCommandManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "consolecommands.yml");
        if (!configFile.exists()) {
            plugin.saveResource("consolecommands.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void runCommands(String path, Player player) {
        if (config.contains(path + ".console")) {
            List<String> consoleCmds = config.getStringList(path + ".console");
            for (String cmd : consoleCmds) {
                if (cmd == null || cmd.trim().isEmpty()) continue; // ✅ Skip invalid lines
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
            }
        }

        if (config.contains(path + ".player")) {
            List<String> playerCmds = config.getStringList(path + ".player");
            for (String cmd : playerCmds) {
                if (cmd == null || cmd.trim().isEmpty()) continue; // ✅ Skip invalid lines
                player.performCommand(cmd.replace("%player%", player.getName()));
            }
        }
    }
}
