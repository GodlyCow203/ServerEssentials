package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class WorldListCommand implements CommandExecutor {

    // Method to get the dynamic prefix
    private String getPrefix() {
        // Replace with your plugin’s way of fetching prefix
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("serveressentials.worldlist")) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "You do not have permission to use /worldlist.");
            return true;
        }

        sender.sendMessage(getPrefix() + ChatColor.GREEN + "Worlds on this server:");

        for (World world : Bukkit.getWorlds()) {
            String name = world.getName();

            boolean loaded = Bukkit.getWorld(name) != null;

            int loadedChunks = loaded ? world.getLoadedChunks().length : 0;
            int entityCount = loaded ? world.getEntities().size() : 0;
            int playerCount = loaded ? world.getPlayers().size() : 0;

            sender.sendMessage(ChatColor.GRAY + " - " + ChatColor.AQUA + name + ChatColor.WHITE + " : " +
                    (loaded ? ChatColor.GREEN + "Loaded" : ChatColor.RED + "Unloaded") +
                    ChatColor.GRAY + " | Chunks: " + ChatColor.YELLOW + loadedChunks +
                    ChatColor.GRAY + " | Entities: " + ChatColor.YELLOW + entityCount +
                    ChatColor.GRAY + " | Players: " + ChatColor.YELLOW + playerCount);
        }

        return true;
    }
}
