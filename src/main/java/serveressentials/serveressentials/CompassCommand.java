package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class CompassCommand implements CommandExecutor {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        float yaw = player.getLocation().getYaw();
        String direction;

        if (yaw < 0) yaw += 360;
        if (yaw >= 337.5 || yaw < 22.5) direction = "North";
        else if (yaw < 67.5) direction = "North-East";
        else if (yaw < 112.5) direction = "East";
        else if (yaw < 157.5) direction = "South-East";
        else if (yaw < 202.5) direction = "South";
        else if (yaw < 247.5) direction = "South-West";
        else if (yaw < 292.5) direction = "West";
        else direction = "North-West";

        player.sendMessage(getPrefix() + ChatColor.GREEN + "You are facing: " + ChatColor.YELLOW + direction);
        return true;
    }
}
