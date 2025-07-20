package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class RankListener implements Listener {

    private final RankManager rankManager;

    public RankListener(RankManager rankManager) {
        this.rankManager = rankManager;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Rank rank = rankManager.getRank(player);

        String prefix = "";
        if (rank != null) {
            prefix = rank.getPrefix();
        }

        event.setFormat(prefix + ChatColor.WHITE + player.getName() + ": " + event.getMessage());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        rankManager.updateTabName(player, rankManager.getRank(player));
    }
}

