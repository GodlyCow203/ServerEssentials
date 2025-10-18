package serveressentials.serveressentials.Player;

import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.PlayerMessages;

public class FeedCommand implements CommandExecutor {

    private final PlayerMessages messages;

    public FeedCommand() {
        this.messages = ServerEssentials.getInstance().getPlayerMessages();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("Feed.only-players"));
            return true;
        }

        player.setFoodLevel(20);
        player.setSaturation(20);
        player.sendMessage(messages.get("Feed.fed"));

        return true;
    }
}
