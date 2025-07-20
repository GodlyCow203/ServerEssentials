package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class SetHomeCommand implements CommandExecutor {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l]&r ");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = getPrefix();

        if (!(sender instanceof Player player)) {
            sender.sendMessage(prefix + ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(prefix + ChatColor.RED + "Usage: /sethome <name>");
            return false;
        }

        // Enforce home limit based on permissions
        int homeLimit = getHomeLimit(player);
        Map<String, Location> existingHomes = HomeManager.getHomes(player.getUniqueId());

        if (existingHomes.size() >= homeLimit) {
            player.sendMessage(prefix + ChatColor.RED + "You have reached your home limit of " + homeLimit + ".");
            return true;
        }

        String homeName = args[0].toLowerCase();
        Location location = player.getLocation();
        HomeManager.setHome(player.getUniqueId(), homeName, location);
        player.sendMessage(prefix + ChatColor.GREEN + "Home '" + homeName + "' set successfully!");
        return true;
    }

    private int getHomeLimit(Player player) {
        int max = 1; // default limit if no permission is found

        for (int i = 1; i <= 100; i++) {
            if (player.hasPermission("serveressentials.sethome." + i)) {
                max = Math.max(max, i);
            }
        }
        return max;
    }
}
