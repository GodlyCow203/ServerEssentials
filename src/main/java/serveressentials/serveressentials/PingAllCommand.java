package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class PingAllCommand implements CommandExecutor {

    private String getPrefix() {
        // Replace with your actual prefix retrieval logic
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l]&r ");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = getPrefix();

        if (!sender.hasPermission("serveressentials.pingall")) {
            sender.sendMessage(prefix + ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        sender.sendMessage(prefix + ChatColor.GREEN + "Player Pings:");
        for (Player player : Bukkit.getOnlinePlayers()) {
            int ping = getPing(player);
            String pingDisplay = ping >= 0 ? ping + "ms" : "N/A";
            sender.sendMessage(ChatColor.YELLOW + player.getName() + ": " + ChatColor.AQUA + pingDisplay);
        }
        return true;
    }

    private int getPing(Player player) {
        try {
            // Get CraftPlayer class
            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + getVersion() + ".entity.CraftPlayer");
            // Cast player to CraftPlayer
            Object craftPlayer = craftPlayerClass.cast(player);
            // Invoke getHandle() method
            Method getHandle = craftPlayerClass.getMethod("getHandle");
            Object entityPlayer = getHandle.invoke(craftPlayer);
            // Get the ping field
            Field pingField = entityPlayer.getClass().getField("ping");
            return pingField.getInt(entityPlayer);
        } catch (Exception e) {
            // e.printStackTrace(); // Uncomment for debugging
            return -1;
        }
    }

    // Helper method to get the server version package name, e.g. v1_19_R1
    private String getVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }
}
