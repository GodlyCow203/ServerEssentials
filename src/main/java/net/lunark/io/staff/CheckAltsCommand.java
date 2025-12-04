package net.lunark.io.staff;


import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CheckAltsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("serveressentials.checkalts")) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("§cUsage: /checkalts <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found or not online.");
            return true;
        }

        String ip = target.getAddress().getAddress().getHostAddress();
        List<String> altAccounts = new ArrayList<>();

        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            if (player.isOnline()) {
                Player online = player.getPlayer();
                if (online != null && online.getAddress() != null &&
                        online.getAddress().getAddress().getHostAddress().equals(ip) &&
                        !online.getName().equals(target.getName())) {
                    altAccounts.add(online.getName());
                }
            }
        }

        if (altAccounts.isEmpty()) {
            sender.sendMessage("§aNo alternate accounts found for §e" + target.getName());
        } else {
            sender.sendMessage("§eAlternate accounts for " + target.getName() + ": §a" + String.join(", ", altAccounts));
        }

        return true;
    }
}
