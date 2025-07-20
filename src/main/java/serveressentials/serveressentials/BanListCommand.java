package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import serveressentials.serveressentials.BanManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

public class BanListCommand implements CommandExecutor {

    private final BanManager banManager;

    public BanListCommand(BanManager banManager) {
        this.banManager = banManager;
    }

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ban.use")) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "You don't have permission.");
            return true;
        }

        Set<String> bannedUUIDs = banManager.getAllBannedUUIDs();
        if (bannedUUIDs.isEmpty()) {
            sender.sendMessage(getPrefix() + ChatColor.YELLOW + "There are no banned players.");
            return true;
        }

        sender.sendMessage(getPrefix() + ChatColor.GOLD + "------[ Banned Players ]------");

        for (String uuid : bannedUUIDs) {
            String name = banManager.getNameFromUUID(uuid);
            String reason = banManager.getReason(uuid);
            long until = banManager.getUntil(uuid);
            String time = (until == -1)
                    ? "Permanent"
                    : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(until));

            sender.sendMessage(getPrefix() + ChatColor.RED + name + ChatColor.GRAY + " - " + ChatColor.WHITE + reason
                    + ChatColor.GRAY + " (" + ChatColor.AQUA + time + ChatColor.GRAY + ")");
        }

        sender.sendMessage(getPrefix() + ChatColor.GOLD + "-----------------------------");
        return true;
    }
}
