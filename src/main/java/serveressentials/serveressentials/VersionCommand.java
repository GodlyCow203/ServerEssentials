package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class VersionCommand implements CommandExecutor {

    // Dynamic prefix getter (adjust based on your main plugin class)
    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String current = VersionChecker.getCurrentVersion();
        String latest = VersionChecker.getLatestVersion();

        sender.sendMessage(getPrefix() + ChatColor.GREEN + "ServerEssentials version: " + current);
        if (!latest.equals("unknown") && !current.equalsIgnoreCase(latest)) {
            sender.sendMessage(getPrefix() + ChatColor.YELLOW + "A new version is available: " + latest);
            sender.sendMessage(getPrefix() + ChatColor.GRAY + "Download: " + ChatColor.AQUA + "https://modrinth.com/plugin/serveressentials");
        } else {
            sender.sendMessage(getPrefix() + ChatColor.GRAY + "You are running the latest version.");
        }

        return true;
    }
}
