package serveressentials.serveressentials.server;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import net.kyori.adventure.text.Component;
import serveressentials.serveressentials.util.ServerMessages;

public class BroadcastCommand implements CommandExecutor {

    private final ServerMessages serverMessages;

    public BroadcastCommand(ServerMessages serverMessages) {
        this.serverMessages = serverMessages;

        // Add default messages if missing
        serverMessages.addDefault("broadcast.no_permission", "<red>You do not have permission to use this command.");
        serverMessages.addDefault("broadcast.usage", "<red>Usage: /broadcast <message>");
        serverMessages.addDefault("broadcast.format", "<blue>[Server] <white>{message}</white>");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!sender.hasPermission("serveressentials.broadcast")) {
            sender.sendMessage(serverMessages.get("broadcast.no_permission"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(serverMessages.get("broadcast.usage"));
            return true;
        }

        String message = String.join(" ", args);

        // Send broadcast with placeholder {message} replaced
        Component broadcast = serverMessages.get("broadcast.format", "{message}", message);
        Bukkit.broadcast(broadcast);

        return true;
    }
}
