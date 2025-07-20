package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class LoadWorldCommand implements CommandExecutor {

    // Dynamic prefix getter (requires ServerEssentials main class with getPrefixConfig())
    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("serveressentials.loadworld")) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(getPrefix() + ChatColor.YELLOW + "Usage: /loadworld <worldname>");
            return true;
        }

        String worldName = args[0];

        if (Bukkit.getWorld(worldName) != null) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "World '" + worldName + "' is already loaded.");
            return true;
        }

        boolean loaded = Bukkit.getServer().createWorld(new WorldCreator(worldName)) != null;

        if (loaded) {
            sender.sendMessage(getPrefix() + ChatColor.GREEN + "World '" + worldName + "' loaded successfully.");
        } else {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Failed to load world '" + worldName + "'. Make sure the world folder exists.");
        }

        return true;
    }
}
