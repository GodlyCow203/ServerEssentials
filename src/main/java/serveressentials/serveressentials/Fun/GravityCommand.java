package serveressentials.serveressentials.Fun;

import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.FunMessages;

public class GravityCommand implements CommandExecutor {

    private final FunMessages messages;

    public GravityCommand() {
        this.messages = new FunMessages(ServerEssentials.getInstance(), "messages/fun.yml");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("Gravity.only-players"));
            return true;
        }

        if (!player.hasPermission("serveressentials.gravity")) {
            player.sendMessage(messages.get("Gravity.no-permission"));
            return true;
        }

        boolean gravity = player.hasGravity();
        player.setGravity(!gravity);
        player.sendMessage(messages.get("Gravity.toggled", "{state}", String.valueOf(!gravity)));

        return true;
    }
}
