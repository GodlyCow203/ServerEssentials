package serveressentials.serveressentials.economy;


import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import serveressentials.serveressentials.ServerEssentials;

public class WhoIsCommand implements CommandExecutor {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(getPrefix() + "Usage: /whois <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(getPrefix() + "Player not found!");
            return true;
        }

        sender.sendMessage(getPrefix() + "Player info:");
        sender.sendMessage(getPrefix() + "Name: " + target.getName());
        sender.sendMessage(getPrefix() + "Health: " + target.getHealth());
        sender.sendMessage(getPrefix() + "Location: " + target.getLocation().toVector());
        return true;
    }
}
