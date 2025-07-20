package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class KickAllCommand implements CommandExecutor {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(sender)) {
                p.kickPlayer(getPrefix() + ChatColor.RED + "You were kicked by an admin.");
            }
        }
        sender.sendMessage(getPrefix() + ChatColor.GREEN + "All players have been kicked.");
        return true;
    }
}
