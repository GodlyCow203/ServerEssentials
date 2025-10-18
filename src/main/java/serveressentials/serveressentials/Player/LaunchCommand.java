package serveressentials.serveressentials.Player;

import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Vector;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.PlayerMessages;

public class LaunchCommand implements CommandExecutor {

    private final PlayerMessages messages;

    public LaunchCommand() {
        this.messages = ServerEssentials.getInstance().getPlayerMessages();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("Launch.only-players"));
            return true;
        }

        Vector launchVelocity = player.getLocation().getDirection().multiply(2).setY(1.5);
        player.setVelocity(launchVelocity);

        player.sendMessage(messages.get("Launch.launched"));
        return true;
    }
}
