package net.lunark.io.mail;

import net.kyori.adventure.text.Component;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MailListener implements Listener {
    private final MailStorage storage;
    private final PlayerLanguageManager langManager;
    private final MailConfig config;
    private final JavaPlugin plugin;
    private final Set<UUID> notifiedPlayers = ConcurrentHashMap.newKeySet();

    public MailListener(MailStorage storage, PlayerLanguageManager langManager, MailConfig config, JavaPlugin plugin) {
        this.storage = storage;
        this.langManager = langManager;
        this.config = config;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        new BukkitRunnable() {
            @Override
            public void run() {
                storage.getUnreadCount(playerId).thenAccept(count -> {
                    if (count > 0 && notifiedPlayers.add(playerId)) {
                        Component msg = langManager.getMessageFor(player, "mail.on-join",
                                "<gold>You have <yellow>{count}</yellow> unread mails! Use <white>/mail read</white>",
                                net.lunark.io.language.LanguageManager.ComponentPlaceholder.of("{count}", String.valueOf(count)));
                        player.sendMessage(msg);
                    }
                });
            }
        }.runTaskLater(plugin, config.joinNotifyDelay);
    }

    public void resetNotification(UUID playerId) {
        notifiedPlayers.remove(playerId);
    }
}