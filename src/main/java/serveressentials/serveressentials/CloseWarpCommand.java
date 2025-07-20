package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static serveressentials.serveressentials.WarpManager.warps;

public class CloseWarpCommand implements CommandExecutor, TabCompleter {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        if (args.length != 1) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Usage: /closewarp <name>");
            return true;
        }

        String name = args[0].toLowerCase();

        if (!WarpManager.warpExists(name)) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Warp " + ChatColor.YELLOW + name + ChatColor.RED + " not found.");
            return true;
        }

        WarpManager.closeWarp(name);
        player.sendMessage(getPrefix() + ChatColor.RED + "Warp " + ChatColor.YELLOW + name + ChatColor.RED + " has been closed.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return Collections.emptyList();

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();




            return completions;
        }

        return Collections.emptyList();
    }
}
