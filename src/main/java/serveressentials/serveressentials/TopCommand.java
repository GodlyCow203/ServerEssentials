package serveressentials.serveressentials;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

public class TopCommand implements CommandExecutor {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l]&r ");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String prefix = getPrefix();

        if (!(sender instanceof Player player)) return false;

        Location loc = player.getLocation();
        for (int y = 255; y > loc.getBlockY(); y--) {
            Location check = new Location(loc.getWorld(), loc.getX(), y, loc.getZ());
            if (check.getBlock().getType() != Material.AIR) {
                player.teleport(check.add(0, 1, 0));
                player.sendMessage(prefix + ChatColor.GREEN + "Teleported to top!");
                return true;
            }
        }
        player.sendMessage(prefix + ChatColor.RED + "No solid block above you found!");
        return true;
    }
}
