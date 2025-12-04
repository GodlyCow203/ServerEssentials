package net.lunark.io.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import net.lunark.io.Managers.SessionManager;

public class PlayerJoinListener implements Listener {

    private final SessionManager sessionManager;

    public PlayerJoinListener(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        sessionManager.startSession(event.getPlayer());
    }

    @EventHandler
    public void onQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        sessionManager.endSession(event.getPlayer());
    }
}
