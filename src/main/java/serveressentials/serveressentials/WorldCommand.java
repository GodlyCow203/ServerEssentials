package serveressentials.serveressentials;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

public class WorldCommand implements CommandExecutor {

    // Dynamic prefix getter (adjust based on your main plugin class)
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

        player.sendMessage(getPrefix() + ChatColor.GREEN + "You are currently in world: " + ChatColor.YELLOW + player.getWorld().getName());
        return true;
    }
}
