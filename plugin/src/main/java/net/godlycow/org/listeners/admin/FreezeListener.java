package net.godlycow.org.listeners.admin;

import net.godlycow.org.commands.impl.FreezeCommand;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class FreezeListener implements Listener {
    private final PlayerLanguageManager langManager;

    public FreezeListener(PlayerLanguageManager langManager) {
        this.langManager = langManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (FreezeCommand.getFrozenPlayers().contains(player.getUniqueId())) {
            if (!event.getFrom().toVector().equals(event.getTo().toVector())) {
                event.setTo(event.getFrom());
            }
        }
    }
}