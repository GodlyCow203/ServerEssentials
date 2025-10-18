package serveressentials.serveressentials.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import serveressentials.serveressentials.Managers.BackManager;

public class BackListener implements Listener {

    // Save location before teleporting
    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        BackManager.setLastLocation(player.getUniqueId(), player.getLocation());
    }

    // Save location on death
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        BackManager.setLastLocation(player.getUniqueId(), player.getLocation());
    }

    // Save location when player quits (so they can back if needed)
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        BackManager.setLastLocation(player.getUniqueId(), player.getLocation());
    }

    // Save location before respawn (so they can back to where they died)
    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        // Only save if we haven't saved already on death
        Location deathLocation = player.getLocation();
        BackManager.setLastLocation(player.getUniqueId(), deathLocation);
    }

    // Optional: Utility method to set back manually (e.g., from a /back command)
    public static void setBack(Player player) {
        Location lastLocation = BackManager.getLastLocation(player.getUniqueId());
        if (lastLocation != null) {
            player.teleport(lastLocation);
        }
    }
}
