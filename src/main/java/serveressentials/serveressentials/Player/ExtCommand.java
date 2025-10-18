package serveressentials.serveressentials.Player;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.PlayerMessages;

public class ExtCommand implements CommandExecutor {

    private final PlayerMessages messages;

    public ExtCommand(ServerEssentials plugin) {
        this.messages = plugin.getPlayerMessages();

        // Add default messages if missing
        messages.addDefault("ext.only-players", "<red>Only players can use this command!");
        messages.addDefault("ext.player-not-found", "<red>Player not found!");
        messages.addDefault("ext.self", "<green>You have been extinguished!");
        messages.addDefault("ext.target", "<green>You extinguished {target}!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("ext.only-players"));
            return true;
        }

        Player player = (Player) sender;
        Player target;

        if (args.length == 0) {
            // Extinguish self
            target = player;
        } else {
            // Extinguish specified player
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null || !target.isOnline()) {
                player.sendMessage(messages.get("ext.player-not-found"));
                return true;
            }
        }

        target.setFireTicks(0); // Extinguish fire

        // Message to extinguished player
        target.sendMessage(messages.get("ext.self"));

        // Message to executor (if different)
        if (!target.equals(player)) {
            player.sendMessage(messages.get("ext.target", "{target}", target.getName()));
        }

        return true;
    }
}
