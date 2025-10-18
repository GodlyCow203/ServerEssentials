package serveressentials.serveressentials.listeners;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import serveressentials.serveressentials.staff.MuteCommand;

import java.util.UUID;

public class ChatListener implements Listener {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        if (MuteCommand.isMuted(uuid)) {
            event.setCancelled(true);

            // Get mute reason
            String reason = MuteCommand.getMuteReason(uuid);

            // Send message directly with MiniMessage formatting

        }
    }
}
