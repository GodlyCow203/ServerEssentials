package net.godlycow.org.listeners.chat;

import net.godlycow.org.mute.storage.MuteStorage;
import net.godlycow.org.language.LanguageManager;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class MuteListener implements Listener {
    private final MuteStorage storage;
    private final PlayerLanguageManager langManager;
    private final Plugin plugin;

    public MuteListener(MuteStorage storage, PlayerLanguageManager langManager, Plugin plugin) {
        this.storage = storage;
        this.langManager = langManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        if (storage.isMutedSync(uuid)) {
            event.setCancelled(true);

            String reason = storage.getMuteReasonSync(uuid).orElse("No reason");

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                event.getPlayer().sendMessage(langManager.getMessageFor(event.getPlayer(),
                        "mute.chat-blocked",
                        "<red>You are muted! Reason: <white>{reason}</white>",
                        LanguageManager.ComponentPlaceholder.of("{reason}", reason)));
            });

            return;
        }
    }
}