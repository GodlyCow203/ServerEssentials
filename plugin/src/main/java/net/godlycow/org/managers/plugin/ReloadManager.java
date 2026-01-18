package net.godlycow.org.managers.plugin;

import net.godlycow.org.EssC;
import org.bukkit.command.CommandSender;

import java.util.LinkedHashMap;
import java.util.Map;

public class ReloadManager {

    private static ReloadManager instance;
    private final EssC plugin;

    private final Map<String, Reloadable> reloadables = new LinkedHashMap<>();

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

    public void register(Reloadable reloadable) {
        reloadables.put(reloadable.name().toLowerCase(), reloadable);
    }

    public static void reloadAll(CommandSender sender) {
        getInstance().reloadEverything(sender);
    }

    public void reloadEverything(CommandSender sender) {
        sender.sendMessage("§6§lReloading modules...");

        reloadables.values().forEach(reloadable -> {
            try {
                reloadable.reload();
                sender.sendMessage("§a✔ " + reloadable.name() + " reloaded!");
            } catch (Exception e) {
                sender.sendMessage("§c✗ " + reloadable.name() + " failed to reload!");
                e.printStackTrace();
            }
        });

        sender.sendMessage("§a§lReload complete.");
    }
}
