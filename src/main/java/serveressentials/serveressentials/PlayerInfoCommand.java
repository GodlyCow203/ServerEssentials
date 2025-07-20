package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerInfoCommand implements CommandExecutor {

    // Dynamic prefix method
    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Usage: /playerinfo <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Player not found.");
            return true;
        }

        sender.sendMessage(getPrefix() + ChatColor.GOLD + "----- Player Info -----");
        sender.sendMessage(getPrefix() + ChatColor.YELLOW + "Name: " + target.getName());
        sender.sendMessage(getPrefix() + ChatColor.YELLOW + "UUID: " + target.getUniqueId());
        sender.sendMessage(getPrefix() + ChatColor.YELLOW + "Health: " + target.getHealth() + "/" + target.getMaxHealth());
        sender.sendMessage(getPrefix() + ChatColor.YELLOW + "Food: " + target.getFoodLevel());
        sender.sendMessage(getPrefix() + ChatColor.YELLOW + "Location: " + target.getLocation().toVector().toString());
        return true;
    }
}
