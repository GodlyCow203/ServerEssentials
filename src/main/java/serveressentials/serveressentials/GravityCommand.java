package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class GravityCommand implements CommandExecutor {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        if (!player.hasPermission("serveressentials.gravity")) {
            player.sendMessage(getPrefix() + ChatColor.RED + "You don't have permission.");
            return true;
        }

        boolean gravity = player.hasGravity();
        player.setGravity(!gravity);
        player.sendMessage(getPrefix() + ChatColor.GREEN + "Gravity toggled " + ChatColor.YELLOW + (!gravity));
        return true;
    }
}
