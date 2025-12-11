package net.lunark.io.listeners;

import net.lunark.io.commands.impl.MuteCommand;
import net.lunark.io.language.LanguageManager;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MuteListener implements Listener {
    private final MuteCommand muteCommand;
    private final PlayerLanguageManager langManager;

    public MuteListener(MuteCommand muteCommand, PlayerLanguageManager langManager) {
        this.muteCommand = muteCommand;
        this.langManager = langManager;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        muteCommand.isMuted(uuid).thenAccept(isMuted -> {
            if (isMuted) {
                muteCommand.getMuteReason(uuid).thenAccept(optReason -> {
                    String reason = optReason.orElse("No reason");
                    event.getPlayer().sendMessage(langManager.getMessageFor(event.getPlayer(),
                            "mute.chat-blocked",
                            "<red>You are muted! Reason: <white>{reason}</white>",
                            LanguageManager.ComponentPlaceholder.of("{reason}", reason)));
                    event.setCancelled(true);
                });
            }
        });
    }
}