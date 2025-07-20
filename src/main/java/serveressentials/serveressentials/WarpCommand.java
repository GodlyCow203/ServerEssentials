package serveressentials.serveressentials;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class WarpCommand implements CommandExecutor, TabCompleter {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig()
                .getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Usage: /warp <name>");
            return true;
        }

        String name = args[0].toLowerCase();

        if (!WarpManager.warpExists(name)) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Warp " + ChatColor.YELLOW + name + ChatColor.RED + " does not exist.");
            return true;
        }

        if (!WarpManager.isWarpEnabled(name)) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Warp " + ChatColor.YELLOW + name + ChatColor.RED + " is currently closed.");
            return true;
        }

        Location warpLoc = WarpManager.getWarp(name);
        if (warpLoc == null) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Warp location is invalid or missing.");
            return true;
        }

        player.teleport(warpLoc);
        player.sendMessage(getPrefix() + ChatColor.GREEN + "Teleported to warp " + ChatColor.YELLOW + name + ChatColor.GREEN + ".");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return List.of();

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();

            for (String warpName : WarpManager.getAllWarpNames()) {
                if (warpName.toLowerCase().startsWith(input)) {
                    completions.add(warpName);
                }
            }
            return completions;
        }

        return List.of();
    }
}
