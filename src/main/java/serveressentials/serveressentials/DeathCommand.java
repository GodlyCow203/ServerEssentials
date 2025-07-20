package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class DeathCommand implements CommandExecutor {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Only players can run this command.");
            return true;
        }

        int deaths = player.getStatistic(Statistic.DEATHS);
        player.sendMessage(getPrefix() + ChatColor.RED + "You have died " + ChatColor.WHITE + deaths + ChatColor.RED + " times.");
        return true;
    }
}
