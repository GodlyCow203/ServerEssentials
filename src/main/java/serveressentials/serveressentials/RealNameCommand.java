package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RealNameCommand implements CommandExecutor {

    // Use dynamic prefix from config
    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Usage: /realname <nickname>");
            return true;
        }

        String nick = args[0];
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getDisplayName().equalsIgnoreCase(nick)) {
                sender.sendMessage(getPrefix() + ChatColor.GREEN + "Real name of " + nick + " is " + ChatColor.YELLOW + p.getName());
                return true;
            }
        }

        sender.sendMessage(getPrefix() + ChatColor.RED + "No player found with nickname " + nick);
        return true;
    }
}
