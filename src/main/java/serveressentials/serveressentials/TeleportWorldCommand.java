package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeleportWorldCommand implements CommandExecutor {

    // Dynamic prefix getter (assumes ServerEssentials main class has getPrefixConfig())
    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("serveressentials.tpp")) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Usage: /tpp <world> <player>");
            return true;
        }

        String worldName = args[0];
        String playerName = args[1];

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "World '" + worldName + "' is not loaded.");
            return true;
        }

        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Player '" + playerName + "' not found.");
            return true;
        }

        target.teleport(world.getSpawnLocation());
        sender.sendMessage(getPrefix() + ChatColor.GREEN + "Teleported " + target.getName() + " to world '" + worldName + "'.");
        target.sendMessage(getPrefix() + ChatColor.AQUA + "You have been teleported to world '" + worldName + "'.");

        return true;
    }
}
