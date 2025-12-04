package net.lunark.io.staff;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Loads fly state when player joins
 */
public class FlyListener implements Listener {
    private final FlyCommand flyCommand;

    public FlyListener(FlyCommand flyCommand) {
        this.flyCommand = flyCommand;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Load flight state async with slight delay to ensure player is fully loaded
        flyCommand.loadFlightState(event.getPlayer());
    }
}