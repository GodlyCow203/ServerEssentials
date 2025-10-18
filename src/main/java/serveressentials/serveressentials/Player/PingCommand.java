package serveressentials.serveressentials.Player;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.PlayerMessages;

public class PingCommand implements CommandExecutor {

    private final PlayerMessages messages;

    public PingCommand(ServerEssentials plugin) {
        this.messages = plugin.getPlayerMessages();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("Ping.only-players"));
            return true;
        }

        int ping = player.getPing();
        player.sendMessage(messages.get("Ping.show", "{ping}", String.valueOf(ping)));
        return true;
    }
}
