package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class FlyCommand implements CommandExecutor {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = getPrefix();

        if (!(sender instanceof Player player)) {
            sender.sendMessage(prefix + ChatColor.RED + "Only players can use this command.");
            return true;
        }

        boolean flying = player.getAllowFlight();
        player.setAllowFlight(!flying);
        player.setFlying(!flying);

        player.sendMessage(prefix + ChatColor.BLUE + "Flight " +
                (flying ? ChatColor.RED + "disabled" : ChatColor.GREEN + "enabled") + ChatColor.BLUE + ".");
        return true;
    }
}
