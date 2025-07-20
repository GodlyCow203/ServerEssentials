package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class EnderSeeCommand implements CommandExecutor {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String prefix = getPrefix();

        if (!(sender instanceof Player player)) return false;

        if (!player.hasPermission("serveressentials.endersee")) {
            player.sendMessage(prefix + ChatColor.RED + "You don't have permission.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(prefix + ChatColor.RED + "Usage: /endersee <player>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage(prefix + ChatColor.RED + "Player not found.");
            return true;
        }

        player.openInventory(target.getEnderChest());
        player.sendMessage(prefix + ChatColor.AQUA + "Opened " + ChatColor.GOLD + target.getName() + ChatColor.AQUA + "'s ender chest.");
        return true;
    }
}
