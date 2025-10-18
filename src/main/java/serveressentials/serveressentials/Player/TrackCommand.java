package serveressentials.serveressentials.Player;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.PlayerMessages;

public class TrackCommand implements CommandExecutor {

    private final PlayerMessages messages;

    public TrackCommand(ServerEssentials plugin) {
        this.messages = plugin.getPlayerMessages(); // Get the PlayerMessages instance
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            // Optional: you can send a message for console
            Component msg = messages.get("Track.only-players");
            sender.sendMessage(msg);
            return true;
        }

        if (args.length != 1) {
            Component usageMsg = messages.get("Track.usage");
            player.sendMessage(usageMsg);
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || !target.isOnline()) {
            Component notFoundMsg = messages.get("Track.not-found", "{target}", args[0]);
            player.sendMessage(notFoundMsg);
            return true;
        }

        Location targetLoc = target.getLocation();
        player.setCompassTarget(targetLoc);
        Component successMsg = messages.get("Track.success", "{target}", target.getName());
        player.sendMessage(successMsg);

        return true;
    }
}
