package serveressentials.serveressentials.staff;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.MessagesManager;

public class KickAllCommand implements CommandExecutor {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        MessagesManager messages = ServerEssentials.getInstance().getMessagesManager();

        if (args.length == 0) {
            sender.sendMessage(messages.getMessageComponent("kickall.usage"));
            return true;
        }

        // Combine all args as the reason
        String reason = String.join(" ", args);

        // Kick message for players
        Component kickMessage = messages.getMessageComponent(
                "kickall.kick-message",
                "%admin%", sender.getName(),
                "%reason%", reason
        );

        int kickedCount = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(sender)) {
                p.kick(kickMessage);
                kickedCount++;
            }
        }

        // Message to sender
        Component senderMessage = messages.getMessageComponent(
                "kickall.sender-message",
                "%count%", String.valueOf(kickedCount),
                "%reason%", reason
        );

        if (sender instanceof Player player) {
            player.sendMessage(senderMessage);
        } else {
            Bukkit.getConsoleSender().sendMessage(miniMessage.serialize(senderMessage));
        }

        return true;
    }
}
