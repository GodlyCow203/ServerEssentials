package net.lunark.io.ban;

import net.kyori.adventure.text.Component;
import net.lunark.io.language.LanguageManager;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.Date;
import java.util.concurrent.CompletableFuture;

public class BanListener implements Listener {
    private final BanStorage storage;
    private final PlayerLanguageManager langManager;

    public BanListener(BanStorage storage, PlayerLanguageManager langManager) {
        this.storage = storage;
        this.langManager = langManager;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        storage.isBanned(event.getPlayer().getUniqueId()).thenAccept(isBanned -> {
            if (isBanned) {
                storage.getBanData(event.getPlayer().getUniqueId()).thenAccept(optData -> {
                    optData.ifPresent(data -> {
                        Component kickMsg = langManager.getMessageFor(null,
                                "ban.kick-message",
                                "<red>You have been banned from <dark_red>{server}</dark_red>\n" +
                                        "<gray>Reason: <yellow>{reason}</yellow>\n" +
                                        "<gray>Banned by: <yellow>{banner}</yellow>\n" +
                                        "<gray>Until: <yellow>{until}</yellow>\n\n" +
                                        "<gray>Appeal at: <aqua>{discord}</aqua>",
                                LanguageManager.ComponentPlaceholder.of("{server}", data.server()),
                                LanguageManager.ComponentPlaceholder.of("{reason}", data.reason()),
                                LanguageManager.ComponentPlaceholder.of("{banner}", data.bannedBy()),
                                LanguageManager.ComponentPlaceholder.of("{until}", data.until() == -1 ? "Permanent" : new Date(data.until()).toString()),
                                LanguageManager.ComponentPlaceholder.of("{discord}", data.discord()));
                        event.disallow(PlayerLoginEvent.Result.KICK_BANNED, kickMsg);
                    });
                });
            }
        });
    }
}