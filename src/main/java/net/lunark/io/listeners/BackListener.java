package net.lunark.io.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import net.lunark.io.Managers.BackManager;

public class BackListener implements Listener {

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        BackManager.setLastLocation(player.getUniqueId(), player.getLocation());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        BackManager.setLastLocation(player.getUniqueId(), player.getLocation());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        BackManager.setLastLocation(player.getUniqueId(), player.getLocation());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Location deathLocation = player.getLocation();
        BackManager.setLastLocation(player.getUniqueId(), deathLocation);
    }

    public static void setBack(Player player) {
        Location lastLocation = BackManager.getLastLocation(player.getUniqueId());
        if (lastLocation != null) {
            player.teleport(lastLocation);
        }
    }
}
