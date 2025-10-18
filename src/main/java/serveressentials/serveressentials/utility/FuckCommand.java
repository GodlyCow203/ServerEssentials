package serveressentials.serveressentials.utility;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class FuckCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("fuck")) {
            sender.sendMessage("§c g9M11c5s Profile Pic looks like a fish"); // §c = red text
            return true;
        }
        return false;
    }
}
