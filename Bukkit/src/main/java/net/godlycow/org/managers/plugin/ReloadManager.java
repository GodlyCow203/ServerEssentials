package net.godlycow.org.managers.plugin;

import org.bukkit.command.CommandSender;
import net.godlycow.org.ServerEssentials;


public class ReloadManager {

    private static ReloadManager instance;
    private final ServerEssentials plugin;

    public ReloadManager(ServerEssentials plugin) {
        this.plugin = plugin;
        instance = this;
    }

    public static ReloadManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ReloadManager has not been initialized yet!");
        }
        return instance;
    }

    public static void reloadAll(CommandSender sender) {
        getInstance().reloadEverything(sender);
    }

    public void reloadEverything(CommandSender sender) {
    }
}