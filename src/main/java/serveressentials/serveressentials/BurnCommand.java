package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class BurnCommand implements CommandExecutor {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("serveressentials.burn")) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "You do not have permission.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Usage: /burn <player> [seconds]");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Player not found.");
            return true;
        }

        if (target.getGameMode() == GameMode.CREATIVE || target.getGameMode() == GameMode.SPECTATOR) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "That player is in a game mode that is immune to fire.");
            return true;
        }

        int seconds = 5; // Default
        if (args.length == 2) {
            try {
                seconds = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(getPrefix() + ChatColor.RED + "Invalid number.");
                return true;
            }
        }

        target.setFireTicks(seconds * 20);
        target.sendMessage(getPrefix() + ChatColor.RED + "You have been set on fire by " + sender.getName() + "!");
        sender.sendMessage(getPrefix() + ChatColor.YELLOW + target.getName() + ChatColor.RED + " has been set on fire for " + ChatColor.YELLOW + seconds + " seconds" + ChatColor.RED + ".");
        return true;
    }
}
