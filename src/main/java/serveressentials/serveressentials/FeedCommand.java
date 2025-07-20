package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FeedCommand implements CommandExecutor {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = getPrefix();

        if (sender instanceof Player player) {
            player.setFoodLevel(20);
            player.setSaturation(20);
            player.sendMessage(prefix + ChatColor.GREEN + "You have been fed!");
        } else {
            sender.sendMessage(prefix + ChatColor.RED + "Only players can use this command.");
        }
        return true;
    }
}
