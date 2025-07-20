package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class UnfreezeCommand implements CommandExecutor {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig()
                .getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Usage: /unfreeze <player>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Player not found or not online.");
            return true;
        }

        // Defensive removal: check if the player was actually frozen
        boolean wasFrozen = FreezeCommand.frozenPlayers.remove(target);
        if (!wasFrozen) {
            player.sendMessage(getPrefix() + ChatColor.YELLOW + target.getName() + " was not frozen.");
            return true;
        }

        player.sendMessage(getPrefix() + ChatColor.GREEN + "Unfroze " + target.getName() + ".");
        target.sendMessage(getPrefix() + ChatColor.GREEN + "You are no longer frozen.");
        return true;
    }
}
