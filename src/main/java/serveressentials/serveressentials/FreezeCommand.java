package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class FreezeCommand implements CommandExecutor {

    public static final Set<Player> frozenPlayers = new HashSet<>();

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = getPrefix();

        if (!(sender instanceof Player)) {
            sender.sendMessage(prefix + ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(prefix + ChatColor.RED + "Usage: /freeze <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            sender.sendMessage(prefix + ChatColor.RED + "Player not found or not online.");
            return true;
        }

        if (frozenPlayers.contains(target)) {
            frozenPlayers.remove(target);
            sender.sendMessage(prefix + ChatColor.YELLOW + "Unfroze " + target.getName() + ".");
            target.sendMessage(prefix + ChatColor.GREEN + "You have been unfrozen.");
        } else {
            frozenPlayers.add(target);
            sender.sendMessage(prefix + ChatColor.GREEN + "Froze " + target.getName() + ".");
            target.sendMessage(prefix + ChatColor.RED + "You have been frozen!");
        }

        return true;
    }
}
