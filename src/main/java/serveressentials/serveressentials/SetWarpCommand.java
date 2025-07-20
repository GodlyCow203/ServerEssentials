package serveressentials.serveressentials;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

public class SetWarpCommand implements CommandExecutor {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l]&r ");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String prefix = getPrefix();

        if (!(sender instanceof Player player)) {
            sender.sendMessage(prefix + ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(prefix + ChatColor.RED + "Usage: /setwarp <name>");
            return true;
        }

        WarpManager.setWarp(args[0].toLowerCase(), player.getLocation());
        player.sendMessage(prefix + ChatColor.GREEN + "Warp " + ChatColor.YELLOW + args[0] + ChatColor.GREEN + " set!");
        return true;
    }
}
