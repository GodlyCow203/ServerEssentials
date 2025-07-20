package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class TpAcceptCommand implements CommandExecutor, TabCompleter {

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
            player.sendMessage(prefix + ChatColor.RED + "Usage: /tpaccept <player>");
            return true;
        }

        Player requester = Bukkit.getPlayerExact(args[0]);
        if (requester == null || !requester.isOnline()) {
            player.sendMessage(prefix + ChatColor.RED + "That player is not online.");
            return true;
        }

        if (!TpaCommand.getRequester(player.getUniqueId()).equals(requester.getUniqueId())) {
            player.sendMessage(prefix + ChatColor.RED + "You have no TPA request from " + requester.getName());
            return true;
        }

        requester.teleport(player);
        requester.sendMessage(prefix + ChatColor.GREEN + "Teleporting to " + player.getName() + "...");
        player.sendMessage(prefix + ChatColor.GREEN + requester.getName() + " has been teleported to you.");

        TpaCommand.removeRequest(player.getUniqueId());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender instanceof Player player) {
            List<String> suggestions = new ArrayList<>();
            UUID targetUUID = player.getUniqueId();
            UUID requesterUUID = TpaCommand.getRequester(targetUUID);

            if (requesterUUID != null) {
                Player requester = Bukkit.getPlayer(requesterUUID);
                if (requester != null) {
                    suggestions.add(requester.getName());
                }
            }
            return suggestions;
        }
        return List.of();
    }
}
