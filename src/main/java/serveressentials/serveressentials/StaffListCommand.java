package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class StaffListCommand implements CommandExecutor {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l]&r ");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String prefix = getPrefix();
        List<String> staffOnline = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("serveressentials.staff")) {
                staffOnline.add(player.getName());
            }
        }

        sender.sendMessage(prefix + ChatColor.AQUA + "--- Online Staff ---");
        if (staffOnline.isEmpty()) {
            sender.sendMessage(prefix + ChatColor.GRAY + "No staff members online.");
        } else {
            for (String name : staffOnline) {
                sender.sendMessage(prefix + ChatColor.GREEN + "• " + ChatColor.YELLOW + name);
            }
        }
        return true;
    }
}
