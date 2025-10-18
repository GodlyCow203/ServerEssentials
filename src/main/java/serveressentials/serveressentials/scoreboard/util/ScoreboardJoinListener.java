package serveressentials.serveressentials.scoreboard.util;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.scoreboard.CustomScoreboardManager;

public class ScoreboardJoinListener implements Listener {

    private final CustomScoreboardManager manager;

    public ScoreboardJoinListener(CustomScoreboardManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String layout = manager.getConfigHandler().getLayoutForPlayer(player, manager.getStorage());

        if (manager.getStorage().isEnabled(player)) {
            manager.getUpdater().update(player, layout);
        } else {
            manager.getUpdater().clear(player);
        }
    }
}
