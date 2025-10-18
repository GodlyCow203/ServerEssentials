package serveressentials.serveressentials.scoreboard;


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

        // Load player data async from DB
        manager.getDatabase().load(uuid).thenAccept(data -> {
            boolean enabled = data.enabled();
            String layout = data.layout();

            // Store into storage.yml as well
            manager.getStorage().setPlayerLayout(player, layout);
            if (!enabled) manager.getStorage().togglePlayer(player); // disable in cache

            // Update scoreboard on main thread
            manager.getPlugin().getServer().getScheduler().runTask(manager.getPlugin(), () -> {
                if (enabled) manager.getUpdater().update(player);
            });
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        boolean enabled = manager.getStorage().isEnabled(player);
        String layout = manager.getStorage().getPlayerLayout(player);

        // Save to DB async
        manager.getDatabase().save(uuid, enabled, layout);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        // Update scoreboard for the new world
        manager.getUpdater().update(player);
    }
}
