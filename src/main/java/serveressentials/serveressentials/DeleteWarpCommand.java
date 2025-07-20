package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DeleteWarpCommand implements CommandExecutor, TabCompleter {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        String prefix = getPrefix();

        if (args.length != 1) {
            player.sendMessage(prefix + ChatColor.RED + "Usage: /deletewarp <name>");
            return true;
        }

        String name = args[0].toLowerCase();
        if (!WarpManager.warpExists(name)) {
            player.sendMessage(prefix + ChatColor.RED + "Warp " + ChatColor.YELLOW + name + ChatColor.RED + " not found.");
            return true;
        }

        WarpManager.deleteWarp(name);
        player.sendMessage(prefix + ChatColor.RED + "Warp " + ChatColor.YELLOW + name + ChatColor.RED + " permanently deleted.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return List.of();

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();

            for (String warp : WarpManager.getWarpNames()) {
                if (warp.toLowerCase().startsWith(input)) {
                    completions.add(warp);
                }
            }
            return completions;
        }

        return List.of();
    }
}
