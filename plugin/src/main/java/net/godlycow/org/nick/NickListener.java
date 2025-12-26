package net.godlycow.org.nick;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class NickListener implements Listener {
    private final NickManager nickManager;

    public NickListener(NickManager nickManager) {
        this.nickManager = nickManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        nickManager.loadPlayerNickname(event.getPlayer().getUniqueId());
    }
}