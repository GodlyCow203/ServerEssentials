package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class TpDenyCommand implements CommandExecutor {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player target)) return true;

        if (!TeleportManager.hasRequest(target)) {
            target.sendMessage(getPrefix() + ChatColor.RED + "No teleport requests.");
            return true;
        }

        Player requester = TeleportManager.getRequester(target);
        if (requester != null && requester.isOnline()) {
            requester.sendMessage(getPrefix() + ChatColor.RED + target.getName() + " denied your teleport request.");
        }

        TeleportManager.removeRequest(target);
        target.sendMessage(getPrefix() + ChatColor.YELLOW + "Teleport request denied.");
        return true;
    }
}
