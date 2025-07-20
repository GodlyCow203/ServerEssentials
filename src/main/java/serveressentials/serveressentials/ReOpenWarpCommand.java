package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class ReOpenWarpCommand implements CommandExecutor {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getConfig().getString("prefix", "&9&l[&bSE&9&l]&r ");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        if (args.length != 1) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Usage: /reopenwarp <name>");
            return true;
        }

        String name = args[0].toLowerCase();

        if (!WarpManager.warpExists(name)) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Warp " + ChatColor.YELLOW + name + ChatColor.RED + " does not exist.");
            return true;
        }

        if (WarpManager.isWarpEnabled(name)) {
            player.sendMessage(getPrefix() + ChatColor.YELLOW + name + ChatColor.RED + " is already open.");
            return true;
        }

        WarpManager.reopenWarp(name);
        player.sendMessage(getPrefix() + ChatColor.GREEN + "Warp " + ChatColor.YELLOW + name + ChatColor.GREEN + " has been reopened.");
        return true;
    }
}
