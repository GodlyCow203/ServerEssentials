package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class BottomCommand implements CommandExecutor {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        Location loc = player.getLocation();
        for (int y = 0; y < loc.getBlockY(); y++) {
            Location check = new Location(loc.getWorld(), loc.getX(), y, loc.getZ());
            if (check.getBlock().getType() != Material.AIR && check.clone().add(0, 1, 0).getBlock().getType() == Material.AIR) {
                player.teleport(check);
                player.sendMessage(getPrefix() + ChatColor.GREEN + "Teleported to bottom!");
                return true;
            }
        }
        player.sendMessage(getPrefix() + ChatColor.RED + "No safe ground below!");
        return true;
    }
}
