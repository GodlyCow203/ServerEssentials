package serveressentials.serveressentials.Player;

import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.PlayerMessages;

public class DayCommand implements CommandExecutor {

    private final PlayerMessages messages;

    public DayCommand() {
        this.messages = ServerEssentials.getInstance().getPlayerMessages();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("Day.only-players"));
            return true;
        }

        player.getWorld().setTime(1000); // Set to day
        player.sendMessage(messages.get("Day.success"));

        return true;
    }
}
