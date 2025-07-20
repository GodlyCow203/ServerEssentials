package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class TpAllCommand implements CommandExecutor {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig()
                .getString("prefix", "&9&l[&bSE&9&l]&r ");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String prefix = getPrefix();

        if (!(sender instanceof Player player)) {
            sender.sendMessage(prefix + ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("admin.tpall")) {
            player.sendMessage(prefix + ChatColor.RED + "You don't have permission.");
            return true;
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(player)) {
                p.teleport(player);
                p.sendMessage(prefix + ChatColor.YELLOW + "You were teleported to " + player.getName() + ".");
            }
        }

        player.sendMessage(prefix + ChatColor.GREEN + "Teleported everyone to you.");
        return true;
    }
}
