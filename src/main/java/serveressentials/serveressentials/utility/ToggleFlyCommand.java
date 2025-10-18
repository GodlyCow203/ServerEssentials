package serveressentials.serveressentials.utility;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.ServerEssentials;

public class ToggleFlyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // If console runs the command
        if (!(sender instanceof Player player)) {
            Component onlyPlayers = ServerEssentials.getInstance()
                    .getMessagesManager()
                    .getMessageComponent("fly.only-players");
            sender.sendMessage(onlyPlayers);
            return true;
        }

        // Permission check
        if (!player.hasPermission("serveressentials.fly")) {
            Component noPerm = ServerEssentials.getInstance()
                    .getMessagesManager()
                    .getMessageComponent("fly.no-permission");
            player.sendMessage(noPerm);
            return true;
        }

        // Toggle flight
        boolean canFly = player.getAllowFlight();
        player.setAllowFlight(!canFly);

        Component enabled = ServerEssentials.getInstance()
                .getMessagesManager()
                .getMessageComponent("fly.enabled", "%player%", player.getName());
        Component disabled = ServerEssentials.getInstance()
                .getMessagesManager()
                .getMessageComponent("fly.disabled", "%player%", player.getName());

        player.sendMessage(player.getAllowFlight() ? enabled : disabled);
        return true;
    }
}
