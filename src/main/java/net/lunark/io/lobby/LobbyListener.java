package net.lunark.io.lobby;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;
import org.bukkit.Location;

public class LobbyListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!LobbyConfig.isTeleportOnJoin()) return;
        if (!player.hasPermission("serveressentials.lobby")) return;

        Location lobby = LobbyStorage.getLobby(player.getWorld().getName());
        if (lobby != null) {
            player.teleport(lobby);
            player.sendMessage(LobbyMessages.get("teleport-lobby"));

            if (LobbyConfig.isAnimationEnabled()) {
            }
        }
    }
}
