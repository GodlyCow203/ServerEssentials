package serveressentials.serveressentials.Player;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.PlayerMessages;

public class WorldCommand implements CommandExecutor {

    private final ServerEssentials plugin;
    private final PlayerMessages messages;

    public WorldCommand(ServerEssentials plugin) {
        this.plugin = plugin;
        this.messages = plugin.getPlayerMessages(); // getter from main class
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            // Message for console/non-player
            Component msg = messages.get("World.only-players");
            sender.sendMessage(msg);
            return true;
        }

        if (args.length > 0) {
            // Invalid usage
            Component usageMsg = messages.get("World.usage",
                    "{player}", player.getName());
            player.sendMessage(usageMsg);
            return true;
        }

        // Message for player showing current world
        Component worldMsg = messages.get("World.current-world",
                "{player}", player.getName(),
                "{world}", player.getWorld().getName());
        player.sendMessage(worldMsg);

        return true;
    }
}
