package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class PingCommand implements CommandExecutor {

    // Dynamic prefix method
    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        int ping = player.getPing();
        player.sendMessage(getPrefix() + ChatColor.YELLOW + "Your ping: " + ping + "ms");
        return true;
    }
}
