package serveressentials.serveressentials;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;

public class UnjailCommand implements CommandExecutor {

    private final Map<UUID, ?> releaseTasks; // Reference to JailCommand's releaseTasks map
    private final Location releaseLocation;

    public UnjailCommand(Map<UUID, ?> releaseTasks, Location releaseLocation) {
        this.releaseTasks = releaseTasks;
        this.releaseLocation = releaseLocation;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!sender.hasPermission("serveressentials.unjail")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /unjail <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found or not online.");
            return true;
        }

        // Cancel scheduled release if exists
        if (releaseTasks.containsKey(target.getUniqueId())) {
            ((BukkitRunnable) releaseTasks.get(target.getUniqueId())).cancel();
            releaseTasks.remove(target.getUniqueId());
        }

        target.teleport(releaseLocation);
        target.sendMessage(ChatColor.GREEN + "You have been released from jail.");

        sender.sendMessage(ChatColor.GREEN + target.getName() + " has been unjailed.");

        return true;
    }
}
