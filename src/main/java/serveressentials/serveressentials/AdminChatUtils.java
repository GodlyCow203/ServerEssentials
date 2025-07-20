package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class AdminChatUtils {

    private static String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    public static void sendAdminMessage(Player sender, String message) {
        String prefix = getPrefix();
        String formatted = prefix + ChatColor.WHITE + sender.getName() + ": " + ChatColor.WHITE + message;

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("serveressentials.adminchat")) {
                online.sendMessage(formatted);
            }
        }

        Bukkit.getConsoleSender().sendMessage(formatted);
    }
}
