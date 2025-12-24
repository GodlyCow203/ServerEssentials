package net.godlycow.org.executor;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CommandExecutor {

    private final Plugin plugin;
    private final File flagFile;
    private final List<String> commands;

    public CommandExecutor(Plugin plugin) {
        this.plugin = plugin;
        this.flagFile = new File(plugin.getDataFolder(), ".installed");


        this.commands = List.of(
                "papi ecloud download Vault"

                );
    }

    public void runIfFirstInstall() {
        if (!flagFile.exists()) {
            plugin.getLogger().info("First install detected – running commands...");
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