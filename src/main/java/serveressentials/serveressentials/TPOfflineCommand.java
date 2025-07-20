package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TPOfflineCommand implements CommandExecutor {

    // Dynamic prefix getter (adjust based on your main plugin class)
    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        if (args.length != 1) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Usage: /tpoffline <player>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        Location loc = target.getPlayer() != null ? target.getPlayer().getLocation() : null;

        if (loc == null) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Player is offline and location is unavailable.");
        } else {
            player.teleport(loc);
            player.sendMessage(getPrefix() + ChatColor.GREEN + "Teleported to " + ChatColor.YELLOW + target.getName());
        }
        return true;
    }
}
