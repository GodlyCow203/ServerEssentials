package serveressentials.serveressentials.Player;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.PlayerMessages;

public class NightCommand implements CommandExecutor {

    private final PlayerMessages messages;

    public NightCommand(ServerEssentials plugin) {
        this.messages = plugin.getPlayerMessages();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("Night.only-players"));
            return true;
        }

        player.getWorld().setTime(13000);
        player.sendMessage(messages.get("Night.success"));
        return true;
    }
}
