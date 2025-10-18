package serveressentials.serveressentials.Player;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.PlayerMessages;

public class TopCommand implements CommandExecutor {

    private final PlayerMessages messages;

    public TopCommand(ServerEssentials plugin) {
        this.messages = plugin.getPlayerMessages();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Component msg = messages.get("Top.only-players");
            sender.sendMessage(msg);
            return true;
        }

        Location loc = player.getLocation();
        boolean teleported = false;

        for (int y = 255; y > loc.getBlockY(); y--) {
            Location check = new Location(loc.getWorld(), loc.getX(), y, loc.getZ());
            if (check.getBlock().getType() != Material.AIR) {
                player.teleport(check.add(0, 1, 0));
                Component successMsg = messages.get("Top.teleported");
                player.sendMessage(successMsg);
                teleported = true;
                break;
            }
        }

        if (!teleported) {
            Component failMsg = messages.get("Top.no-block");
            player.sendMessage(failMsg);
        }

        return true;
    }
}
