package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UnloadWorldCommand implements CommandExecutor, TabCompleter {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length != 1) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Usage: /unloadworld <world_name>");
            return true;
        }

        String worldName = args[0];
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "World '" + worldName + "' not found.");
            return true;
        }

        if (!Bukkit.unloadWorld(world, true)) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Failed to unload world '" + worldName + "'.");
            return true;
        }

        sender.sendMessage(getPrefix() + ChatColor.GREEN + "World '" + worldName + "' has been unloaded successfully.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> worldNames = new ArrayList<>();
            for (World world : Bukkit.getWorlds()) {
                worldNames.add(world.getName());
            }

            String input = args[0].toLowerCase();
            worldNames.removeIf(name -> !name.toLowerCase().startsWith(input));
            return worldNames;
        }
        return Collections.emptyList();
    }
}
