package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KillCommand implements CommandExecutor {

    private final KillTracker killTracker;

    public KillCommand(KillTracker killTracker) {
        this.killTracker = killTracker;
    }

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Only players can use this command.");
            return true;
        }

        int kills = killTracker.getKills(player);
        player.sendMessage(getPrefix() + ChatColor.GREEN + "You have " + ChatColor.GOLD + kills + ChatColor.GREEN + " player kills.");
        return true;
    }
}
