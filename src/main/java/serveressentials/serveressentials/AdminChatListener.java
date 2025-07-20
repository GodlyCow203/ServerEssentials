package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class AdminChatListener implements Listener {

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (AdminChatCommand.isInAdminChat(player)) {
            event.setCancelled(true);
            AdminChatUtils.sendAdminMessage(player, event.getMessage());
        }
    }
}
