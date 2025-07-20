package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class UptimeCommand implements CommandExecutor {

    private final long serverStartTime;

    public UptimeCommand(long serverStartTime) {
        this.serverStartTime = serverStartTime;
    }

    // Dynamic prefix getter, replace with your plugin’s prefix fetching method
    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        long currentTime = System.currentTimeMillis();
        long uptimeMillis = currentTime - serverStartTime;

        long seconds = uptimeMillis / 1000 % 60;
        long minutes = uptimeMillis / (1000 * 60) % 60;
        long hours = uptimeMillis / (1000 * 60 * 60) % 24;
        long days = uptimeMillis / (1000 * 60 * 60 * 24);

        String uptime = String.format("%d days, %d hours, %d minutes, %d seconds", days, hours, minutes, seconds);

        sender.sendMessage(getPrefix() + ChatColor.GREEN + "Server Uptime: " + ChatColor.YELLOW + uptime);
        return true;
    }
}
