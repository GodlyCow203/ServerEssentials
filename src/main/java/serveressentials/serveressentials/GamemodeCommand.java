package serveressentials.serveressentials;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.GamemodeGUI;

public class GamemodeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("gamemode.gui")) {
            player.sendMessage("§cYou do not have permission.");
            return true;
        }

        GamemodeGUI.openGamemodeGUI(player);
        return true;
    }
}
