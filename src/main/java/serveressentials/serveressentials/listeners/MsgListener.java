package serveressentials.serveressentials.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import serveressentials.serveressentials.Player.MsgToggleCommand;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.PlayerMessages;

public class MsgListener implements Listener {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final PlayerMessages messages;

    public MsgListener(ServerEssentials plugin) {
        this.messages = plugin.getPlayerMessages();
    }

    @EventHandler
    public void onPrivateMessage(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage().toLowerCase();

        if (message.startsWith("/msg ") || message.startsWith("/tell ") ||
                message.startsWith("/whisper ") || message.startsWith("/w ")) {

            String[] args = event.getMessage().split(" ");
            if (args.length < 2) return;

            Player sender = event.getPlayer();
            Player target = sender.getServer().getPlayerExact(args[1]);

            if (target != null && MsgToggleCommand.hasMessagesDisabled(target)) {
                Component msg = messages.get("MsgToggle.DisabledTarget", "{player}", target.getName());
                sender.sendMessage(msg);
                event.setCancelled(true);
            }
        }
    }
}
