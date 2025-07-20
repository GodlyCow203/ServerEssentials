package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class AdminChatCommand implements CommandExecutor {

    private static final Set<Player> toggled = new HashSet<>();

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

        if (!player.hasPermission("serveressentials.adminchat")) {
            player.sendMessage(getPrefix() + ChatColor.RED + "You do not have permission to use admin chat.");
            return true;
        }

        if (args.length == 0) {
            if (toggled.contains(player)) {
                toggled.remove(player);
                player.sendMessage(getPrefix() + ChatColor.GRAY + "Admin chat toggled " + ChatColor.RED + "OFF");
            } else {
                toggled.add(player);
                player.sendMessage(getPrefix() + ChatColor.GRAY + "Admin chat toggled " + ChatColor.GREEN + "ON");
            }
        } else {
            String message = String.join(" ", args);
            AdminChatUtils.sendAdminMessage(player, message);
        }

        return true;
    }

    public static boolean isInAdminChat(Player player) {
        return toggled.contains(player);
    }
}
