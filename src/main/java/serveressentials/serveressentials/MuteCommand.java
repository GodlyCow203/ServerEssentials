package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MuteCommand implements CommandExecutor {

    public static final Set<UUID> mutedPlayers = new HashSet<>();

    // Dynamic prefix method
    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("serveressentials.mute")) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "No permission.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Usage: /mute <player>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Player not found.");
            return true;
        }

        mutedPlayers.add(target.getUniqueId());
        sender.sendMessage(getPrefix() + ChatColor.YELLOW + "Muted " + ChatColor.GREEN + target.getName());
        return true;
    }
}
