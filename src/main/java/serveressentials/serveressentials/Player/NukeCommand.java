package serveressentials.serveressentials.Player;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.FunMessages;

public class NukeCommand implements CommandExecutor {

    private final FunMessages messages;

    public NukeCommand(ServerEssentials plugin) {
        this.messages = new FunMessages(plugin, "messages/fun.yml");

        // Add defaults if missing
        messages.addDefault("nuke.only-players", "<red>Only players can use this command!");
        messages.addDefault("nuke.deployed", "<green>Nuke deployed!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("nuke.only-players"));
            return true;
        }

        Player player = (Player) sender;
        World world = player.getWorld();
        Location loc = player.getLocation();

        // Create explosion
        world.createExplosion(loc, 5F, true, true);

        // Send message
        player.sendMessage(messages.get("nuke.deployed"));
        return true;
    }
}
