package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

public class TrackCommand implements CommandExecutor {

    // Dynamic prefix getter (adjust based on your main plugin class)
    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length != 1) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Usage: /track <player>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Player not found.");
            return true;
        }

        Location targetLoc = target.getLocation();
        player.setCompassTarget(targetLoc);
        player.sendMessage(getPrefix() + ChatColor.GREEN + "Compass is now tracking " + ChatColor.YELLOW + target.getName() + ChatColor.GREEN + ".");
        return true;
    }
}
