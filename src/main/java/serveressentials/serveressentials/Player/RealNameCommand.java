package serveressentials.serveressentials.Player;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.PlayerMessages;

public class RealNameCommand implements CommandExecutor {

    private final PlayerMessages messages;

    public RealNameCommand(ServerEssentials plugin) {
        this.messages = plugin.getPlayerMessages();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("RealName.only-players"));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(messages.get("RealName.usage"));
            return true;
        }

        String nick = args[0];
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getDisplayName().equalsIgnoreCase(nick)) {
                player.sendMessage(messages.get("RealName.found", "{nickname}", nick, "{realname}", p.getName()));
                return true;
            }
        }

        player.sendMessage(messages.get("RealName.not-found", "{nickname}", nick));
        return true;
    }
}
