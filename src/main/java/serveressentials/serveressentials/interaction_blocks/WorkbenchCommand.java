package serveressentials.serveressentials.interaction_blocks;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import serveressentials.serveressentials.ServerEssentials;

public class WorkbenchCommand implements CommandExecutor {

    // Dynamic prefix getter (adjust based on your main plugin class)
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

        if (!player.hasPermission("serveressentials.workbench")) {
            player.sendMessage(getPrefix() + ChatColor.RED + "You don't have permission.");
            return true;
        }

        player.openWorkbench(null, true);
        player.sendMessage(getPrefix() + ChatColor.GREEN + "Opened workbench.");
        return true;
    }
}
