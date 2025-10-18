package serveressentials.serveressentials.Player;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.util.PlayerMessages;

import java.util.HashMap;
import java.util.UUID;

public class ReplyCommand implements CommandExecutor {

    private final HashMap<UUID, UUID> lastMessageMap;
    private final PlayerMessages messages;

    public ReplyCommand(HashMap<UUID, UUID> lastMessageMap, PlayerMessages messages) {
        this.lastMessageMap = lastMessageMap;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            Component msg = messages.get("reply.only-players");
            sender.sendMessage(msg);
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(messages.get("reply.usage"));
            return true;
        }

        if (!lastMessageMap.containsKey(player.getUniqueId())) {
            player.sendMessage(messages.get("reply.no-reply"));
            return true;
        }

        UUID targetUUID = lastMessageMap.get(player.getUniqueId());
        Player target = Bukkit.getPlayer(targetUUID);

        if (target == null || !target.isOnline()) {
            player.sendMessage(messages.get("reply.target-offline"));
            return true;
        }

        String message = String.join(" ", args);

        Component senderMsg = messages.get("reply.sender",
                "<target>", target.getName(),
                "<message>", message);
        player.sendMessage(senderMsg);

        Component receiverMsg = messages.get("reply.receiver",
                "<sender>", player.getName(),
                "<message>", message);
        target.sendMessage(receiverMsg);

        // Update reply map so target can reply back
        lastMessageMap.put(target.getUniqueId(), player.getUniqueId());

        return true;
    }
}
