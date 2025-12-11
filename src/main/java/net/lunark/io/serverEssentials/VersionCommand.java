package net.lunark.io.serverEssentials;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import net.lunark.io.ServerEssentials;

public class VersionCommand implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String current = VersionChecker.getCurrentVersion();
        String latest = VersionChecker.getLatestVersion();

        sender.sendMessage(ChatColor.DARK_GRAY + "==============================");
        sender.sendMessage(ChatColor.BLUE + "   ServerEssentials");
        sender.sendMessage(ChatColor.DARK_GRAY + "------------------------------");
        sender.sendMessage(ChatColor.GRAY + "Current Version: " + ChatColor.RED + current);

        if (!latest.equals("unknown") && !current.equalsIgnoreCase(latest)) {
            sender.sendMessage(ChatColor.GRAY + "Latest Version: " + ChatColor.GREEN + latest);
            sender.sendMessage(ChatColor.GRAY + "Download: " + ChatColor.BLUE + "https://modrinth.com/plugin/serveressentials");
        } else {
            sender.sendMessage(ChatColor.WHITE + "You are running the latest &8version.");
        }

        sender.sendMessage(ChatColor.DARK_GRAY + "==============================");
        return true;
    }
}
