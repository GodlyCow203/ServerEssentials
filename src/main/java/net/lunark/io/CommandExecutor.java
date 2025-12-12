package net.lunark.io;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Runs one-time in-game commands the very first time the plugin is installed.
 * Add the commands you want to the 'first-install-commands' list in config.yml
 * or edit them directly below.
 */
public class FirstInstallCommands {

    private final Plugin plugin;
    private final File flagFile;
    private final List<String> commands;

    public FirstInstallCommands(Plugin plugin) {
        this.plugin = plugin;
        this.flagFile = new File(plugin.getDataFolder(), ".installed");

        // You can hard-code commands here OR read them from config.yml
        // Option 1: hard-coded (remove/comment if you use config)
        this.commands = List.of(
                "papi ecloud download Server",
                "papi ecloud download Player",
                "papi ecloud download Vault",
                "papi ecloud download Statistic"




                );


    }

    public void runIfFirstInstall() {
        if (!flagFile.exists()) {
            plugin.getLogger().info("First install detected – running welcome commands...");
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (String cmd : commands) {
                    if (cmd != null && !cmd.isBlank()) {
                        String parsed = cmd.replace("@a", "@a").replace("@p", "@a");
                        boolean ok = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
                        if (!ok) {
                            plugin.getLogger().warning("First-install command failed: " + parsed);
                        }
                    }
                }
                markAsInstalled();
            }, 20L);
        }
    }


    private void markAsInstalled() {
        try {
            flagFile.getParentFile().mkdirs();
            flagFile.createNewFile();
        } catch (IOException e) {
            plugin.getLogger().severe("Could not create first-install flag file – commands may re-run on restart!");
            e.printStackTrace();
        }
    }
}