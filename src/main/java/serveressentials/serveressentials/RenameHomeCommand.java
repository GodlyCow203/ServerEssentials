package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RenameHomeCommand implements CommandExecutor {

    // Use the same dynamic prefix method
    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Usage: /renamehome <oldname> <newname>");
            return true;
        }

        if (!HomeManager.renameHome(player.getUniqueId(), args[0], args[1])) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Rename failed. Check names.");
        } else {
            player.sendMessage(getPrefix() + ChatColor.GREEN + "Home renamed from '" + args[0] + "' to '" + args[1] + "'!");
        }

        return true;
    }
}
