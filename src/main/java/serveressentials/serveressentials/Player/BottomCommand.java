package serveressentials.serveressentials.Player;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.util.PlayerMessages;

public class BottomCommand implements CommandExecutor {

    private final PlayerMessages playerMessages;

    public BottomCommand(PlayerMessages playerMessages) {
        this.playerMessages = playerMessages;

        // Add default messages if missing
        playerMessages.addDefault("bottom.success", "<green>Teleported to bottom!");
        playerMessages.addDefault("bottom.no_safe_ground", "<red>No safe ground below!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        Location loc = player.getLocation();
        for (int y = 0; y < loc.getBlockY(); y++) {
            Location check = new Location(loc.getWorld(), loc.getX(), y, loc.getZ());
            if (check.getBlock().getType() != Material.AIR && check.clone().add(0, 1, 0).getBlock().getType() == Material.AIR) {
                player.teleport(check);
                player.sendMessage(playerMessages.get("bottom.success"));
                return true;
            }
        }

        player.sendMessage(playerMessages.get("bottom.no_safe_ground"));
        return true;
    }
}
