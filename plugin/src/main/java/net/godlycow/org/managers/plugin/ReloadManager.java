package net.godlycow.org.managers.plugin;

import org.bukkit.command.CommandSender;
import net.godlycow.org.EssC;


public class ReloadManager {

    private static ReloadManager instance;
    private final EssC plugin;

    public ReloadManager(EssC plugin) {
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
        if (plugin.getHomeManager() != null) {
            plugin.getHomeManager().reload();
            sender.sendMessage("§a✔ Homes system reloaded!");
        } else {
            sender.sendMessage("§c✗ Homes system not available!");
        }

    }
}