package net.godlycow.org.ban.trigger;

import net.godlycow.org.ban.storage.BanStorage;
import net.kyori.adventure.text.Component;
import net.godlycow.org.language.LanguageManager;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.Plugin;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class BanListener implements Listener {
    private final BanStorage storage;
    private final PlayerLanguageManager langManager;
    private final Plugin plugin;

    public BanListener(BanStorage storage, PlayerLanguageManager langManager, Plugin plugin) {
        this.storage = storage;
        this.langManager = langManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        if (storage.isBannedSync(uuid)) {
            Optional<BanStorage.BanData> optData = storage.getBanDataSync(uuid);

            if (optData.isPresent()) {
                BanStorage.BanData data = optData.get();

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
                        LanguageManager.ComponentPlaceholder.of("{until}",
                                data.until() == -1 ? "Permanent" : new Date(data.until()).toString()),
                        LanguageManager.ComponentPlaceholder.of("{discord}", data.discord()));

                event.disallow(PlayerLoginEvent.Result.KICK_BANNED, kickMsg);
            }
        }
    }
}