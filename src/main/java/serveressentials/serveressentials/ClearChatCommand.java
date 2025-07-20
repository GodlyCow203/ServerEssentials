package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClearChatCommand implements CommandExecutor {

    private String getPrefix() {
        // Adjust this to your actual method to get the prefix from your plugin
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l]&r ");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        String prefix = getPrefix();

        if (!sender.hasPermission("serveressentials.clearchat")) {
            sender.sendMessage(prefix + ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        String clearedBy = (sender instanceof Player)
                ? ((Player) sender).getDisplayName()
                : "Console";

        for (Player player : Bukkit.getOnlinePlayers()) {
            for (int i = 0; i < 150; i++) {
                player.sendMessage(""); // Clears chat by flooding with empty lines
            }
            player.sendMessage(prefix + ChatColor.GRAY + "Chat has been cleared by " + ChatColor.RED + clearedBy);
        }

        return true;
    }
}
