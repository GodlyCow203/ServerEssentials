package net.godlycow.org.listeners.admin;

import net.godlycow.org.commands.impl.FlyCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class FlyListener implements Listener {
    private final FlyCommand flyCommand;

    public FlyListener(FlyCommand flyCommand) {
        this.flyCommand = flyCommand;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        flyCommand.loadFlightState(event.getPlayer());
    }
}