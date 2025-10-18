package serveressentials.serveressentials.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.staff.FreezeCommand;

public class FreezeListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (FreezeCommand.frozenPlayers.contains(player)) {
            if (!event.getFrom().toVector().equals(event.getTo().toVector())) {
                event.setTo(event.getFrom());
            }
        }
    }
}
