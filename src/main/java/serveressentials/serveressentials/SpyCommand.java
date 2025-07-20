package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class SpyCommand implements CommandExecutor {

    // Set to keep track of players who have spy enabled
    private static final Set<Player> spies = new HashSet<>();

    private String getPrefix() {
        // Replace with your actual prefix logic or plugin instance call
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l Spy] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("serveressentials.spy")) {
            player.sendMessage(getPrefix() + ChatColor.RED + "You do not have permission to use /spy.");
            return true;
        }

        if (spies.contains(player)) {
            spies.remove(player);
            player.sendMessage(getPrefix() + ChatColor.GREEN + "Spy mode disabled.");
        } else {
            spies.add(player);
            player.sendMessage(getPrefix() + ChatColor.GREEN + "Spy mode enabled.");
        }

        return true;
    }

    // Public getter for other classes to check if a player is spying
    public static boolean isSpying(Player player) {
        return spies.contains(player);
    }

    // Get all spying players (to send spy messages)
    public static Set<Player> getSpies() {
        return spies;
    }
}
