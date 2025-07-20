package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import serveressentials.serveressentials.BanManager;

import java.util.UUID;

public class UnbanCommand implements CommandExecutor {

    private final BanManager banManager;

    public UnbanCommand(BanManager banManager) {
        this.banManager = banManager;
    }

    // Dynamic prefix getter (adjust based on your main plugin class)
    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ban.use")) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "You don't have permission.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Usage: /unban <player>");
            return true;
        }

        String playerName = args[0];
        UUID uuid = banManager.getUUIDFromName(playerName);

        if (uuid == null) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Could not find banned player with name '" + playerName + "'.");
            return true;
        }

        if (!banManager.isBanned(uuid)) {
            sender.sendMessage(getPrefix() + ChatColor.YELLOW + playerName + " is not currently banned.");
            return true;
        }

        banManager.unbanPlayer(uuid);
        sender.sendMessage(getPrefix() + ChatColor.GREEN + playerName + " has been unbanned.");
        return true;
    }
}
