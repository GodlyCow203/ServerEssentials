package net.godlycow.org.executor;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FirstInstallCommandRunner {

    private final Plugin plugin;
    private final File flagFile;
    private final List<String> commands;

    public FirstInstallCommandRunner(Plugin plugin) {
        this.plugin = plugin;
        this.flagFile = new File(plugin.getDataFolder(), ".installed");

        this.commands = List.of(
                "papi ecloud download Vault",
                "papi ecloud download Statistic",
                "papi ecloud download Server",
                "language reload"
        );
    }

    public void runIfFirstInstall() {
        if (!flagFile.exists()) {
            plugin.getLogger().info("First install detected – running commands...");

            Bukkit.getScheduler().runTaskLater(plugin, () -> {

                if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
                    plugin.getLogger().severe("PlaceholderAPI not found! Skipping first-install commands.");
                    return;
                }

                long delay = 0L;

                for (String cmd : commands) {
                    long finalDelay = delay;
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                    }, finalDelay);

                    delay += 20L * 3;
                }

                Bukkit.getScheduler().runTaskLater(plugin, this::markAsInstalled, delay + 20L);

            }, 20L * 10);
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
