package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class DeleteHomeCommand implements CommandExecutor, TabCompleter {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        String prefix = getPrefix();

        if (args.length != 1) {
            player.sendMessage(prefix + ChatColor.RED + "Usage: /deletehome <name>");
            return true;
        }

        if (!HomeManager.deleteHome(player.getUniqueId(), args[0])) {
            player.sendMessage(prefix + ChatColor.RED + "Couldn't delete. Maybe it doesn't exist?");
        } else {
            player.sendMessage(prefix + ChatColor.GREEN + "Home '" + args[0] + "' deleted.");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) return List.of();

        if (args.length == 1) {
            UUID playerUUID = player.getUniqueId();

            // Suppose HomeManager.getHomes returns Set<String> or List<String>, adapt accordingly:
            Map<String, Location> homesMap = HomeManager.getHomes(playerUUID);
            Set<String> homesSet = homesMap.keySet();
            List<String> homes = new ArrayList<>(homesSet);
// if Set<String>


            List<String> completions = new ArrayList<>();
            String input = args[0].toLowerCase();

            for (String home : homes) {
                if (home.toLowerCase().startsWith(input)) {
                    completions.add(home);
                }
            }
            return completions;
        }
        return List.of();
    }
}
