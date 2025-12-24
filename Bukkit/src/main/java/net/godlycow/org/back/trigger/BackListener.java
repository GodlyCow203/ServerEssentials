package net.godlycow.org.back.trigger;

import net.godlycow.org.back.BackManager;
import net.godlycow.org.commands.config.BackConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.Location;


public class BackListener implements Listener {
    private final BackManager backManager;
    private final BackConfig config;

    public BackListener(BackManager backManager, BackConfig config) {
        this.backManager = backManager;
        this.config = config;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        backManager.hasBack(player.getUniqueId()).thenAccept(has -> {
            if (has) {
                Bukkit.getLogger().info("Player " + player.getName() + " has a saved back location");
            }
        });
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (!config.saveOnTeleport()) return;

        Player player = event.getPlayer();
        if (event.getFrom() != null && event.getFrom().getWorld() != null) {
            backManager.setLastLocation(player.getUniqueId(), event.getFrom()).thenRun(() -> {
                Bukkit.getLogger().fine("Saved back location for " + player.getName());
            });
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!config.saveOnDeath()) return;

        Player player = event.getEntity();
        backManager.setLastLocation(player.getUniqueId(), player.getLocation());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (!config.saveOnQuit()) return;

        Player player = event.getPlayer();
        backManager.setLastLocation(player.getUniqueId(), player.getLocation());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (!config.saveOnRespawn()) return;

        Player player = event.getPlayer();
        Location deathLocation = player.getLastDeathLocation();
        if (deathLocation != null) {
            backManager.setLastLocation(player.getUniqueId(), deathLocation);
        }
    }
}