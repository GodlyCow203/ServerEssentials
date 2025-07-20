package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class UnmuteCommand implements CommandExecutor {

    // Dynamic prefix getter (adjust based on your main plugin class)
    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("serveressentials.unmute")) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "No permission.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Usage: /unmute <player>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Player not found.");
            return true;
        }

        if (MuteCommand.mutedPlayers.remove(target.getUniqueId())) {
            sender.sendMessage(getPrefix() + ChatColor.GREEN + "Unmuted " + ChatColor.AQUA + target.getName());
        } else {
            sender.sendMessage(getPrefix() + ChatColor.RED + "That player isn't muted.");
        }
        return true;
    }
}
