package serveressentials.serveressentials.Player;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.PlayerMessages;

public class SuicideCommand implements CommandExecutor {

    private final PlayerMessages messages;

    public SuicideCommand(ServerEssentials plugin) {
        this.messages = plugin.getPlayerMessages();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            Component msg = messages.get("Suicide.only-players");
            sender.sendMessage(msg);
            return true;
        }

        player.setHealth(0); // Kill the player
        Component suicideMsg = messages.get("Suicide.success");
        player.sendMessage(suicideMsg);

        return true;
    }
}
