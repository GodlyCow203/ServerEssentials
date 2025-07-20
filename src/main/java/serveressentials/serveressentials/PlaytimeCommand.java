package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlaytimeCommand implements CommandExecutor {

    // Dynamic prefix getter (adjust based on your main plugin class)
    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            long ticks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
            long minutes = ticks / (20 * 60);
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;

            player.sendMessage(getPrefix() + ChatColor.AQUA + "Playtime: " + hours + "h " + remainingMinutes + "m");
        } else {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Only players can use this command.");
        }
        return true;
    }
}
