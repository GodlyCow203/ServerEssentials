package net.lunark.io.server;

import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import net.lunark.io.util.ServerMessages;

public class LoadWorldCommand implements CommandExecutor {

    private final ServerMessages messages;

    public LoadWorldCommand(ServerMessages messages) {
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("serveressentials.loadworld")) {
            sender.sendMessage(messages.get("loadworld.no-permission"));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(messages.get("loadworld.usage"));
            return true;
        }

        String worldName = args[0];

        if (Bukkit.getWorld(worldName) != null) {
            sender.sendMessage(messages.get("loadworld.already-loaded", "<world>", worldName));
            return true;
        }

        boolean loaded = Bukkit.getServer().createWorld(new WorldCreator(worldName)) != null;

        if (loaded) {
            sender.sendMessage(messages.get("loadworld.success", "<world>", worldName));
        } else {
            sender.sendMessage(messages.get("loadworld.failed", "<world>", worldName));
        }

        return true;
    }
}
