package net.lunark.io.scoreboard;


import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class ScoreboardListener implements Listener {

    private final CustomScoreboardManager manager;

    public ScoreboardListener(CustomScoreboardManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        manager.getDatabase().load(uuid).thenAccept(data -> {
            boolean enabled = data.enabled();
            String layout = data.layout();

            manager.getStorage().setPlayerLayout(player, layout);
            manager.getStorage().setCachedState(uuid, enabled);

            manager.getPlugin().getServer().getScheduler().runTask(manager.getPlugin(), () -> {
                if (enabled) manager.getUpdater().update(player);
                else         manager.getUpdater().clear(player);
            });
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        boolean enabled = manager.getStorage().isEnabled(player);
        String layout  = manager.getStorage().getPlayerLayout(player);

        manager.getDatabase().save(uuid, enabled, layout);

        manager.getStorage().removeCachedState(uuid);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        manager.getUpdater().update(player);
    }
}
