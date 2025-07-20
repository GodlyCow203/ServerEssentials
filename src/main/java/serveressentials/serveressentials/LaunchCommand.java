package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class LaunchCommand implements CommandExecutor {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            Vector launchVelocity = player.getLocation().getDirection().multiply(2).setY(1.5);
            player.setVelocity(launchVelocity);
            player.sendMessage(getPrefix() + ChatColor.LIGHT_PURPLE + "Woosh! You’ve been launched!");
        } else {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Only players can use this command.");
        }
        return true;
    }
}
