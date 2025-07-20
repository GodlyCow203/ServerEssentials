package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class SwapCommand implements CommandExecutor {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l]&r ");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = getPrefix();

        if (!(sender instanceof Player player)) return false;
        if (args.length != 1) {
            player.sendMessage(prefix + ChatColor.RED + "Usage: /swap <player>");
            return true;
        }

        Player p2 = Bukkit.getPlayerExact(args[0]);

        if (p2 == null || p2.equals(player)) {
            player.sendMessage(prefix + ChatColor.RED + "Player not found or invalid.");
            return true;
        }

        var loc1 = player.getLocation();
        var loc2 = p2.getLocation();

        player.teleport(loc2);
        p2.teleport(loc1);

        player.sendMessage(prefix + ChatColor.GREEN + "You swapped with " + ChatColor.YELLOW + p2.getName());
        p2.sendMessage(prefix + ChatColor.GREEN + "You swapped with " + ChatColor.YELLOW + player.getName());
        return true;
    }
}
