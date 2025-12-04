package net.lunark.io.staff;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class BanListener implements Listener {

    private final BanManager banManager;

    public BanListener(BanManager banManager) {
        this.banManager = banManager;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (banManager.isBanned(event.getPlayer().getUniqueId())) {
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED,
                    banManager.getBanMessage(event.getPlayer().getUniqueId()));
        }
    }
}
