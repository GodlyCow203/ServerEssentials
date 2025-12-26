package net.godlycow.org.scoreboard.runtime;

import net.godlycow.org.commands.config.ScoreboardConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import net.godlycow.org.scoreboard.storage.ScoreboardStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public final class ScoreboardListener implements Listener {
    private final Plugin plugin;
    private final PlayerLanguageManager langManager;
    private final ScoreboardConfig config;
    private final ScoreboardStorage storage;
    private final ScoreboardUpdater updater;

    public ScoreboardListener(Plugin plugin, PlayerLanguageManager langManager, ScoreboardConfig config,
                              ScoreboardStorage storage, ScoreboardUpdater updater) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.config = config;
        this.storage = storage;
        this.updater = updater;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!config.enabled) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        CompletableFuture.runAsync(() -> {
            try {
                storage.loadPlayer(uuid).thenAccept(data -> {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (!config.isWorldEnabled(player.getWorld().getName())) {
                            updater.clear(player);
                            return;
                        }

                        String layout = data.layout() != null ? data.layout() :
                                config.getWorldLayout(player.getWorld().getName());
                        if (layout == null) layout = config.defaultLayout;

                        if (data.enabled()) {
                            updater.update(player, layout);
                        }
                    });
                }).exceptionally(ex -> {
                    plugin.getLogger().log(Level.WARNING,
                            "Failed to load scoreboard data for " + player.getName(), ex);
                    return null;
                });
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE,
                        "Critical error in scoreboard join handler", e);
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (!config.enabled) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        boolean enabled = storage.isEnabled(uuid);
        String layout = storage.getLayout(uuid);

        storage.savePlayer(uuid, enabled, layout).thenAccept(v -> {
            storage.removeFromCache(uuid);
        }).exceptionally(ex -> {
            plugin.getLogger().log(Level.WARNING,
                    "Failed to save scoreboard data for " + player.getName(), ex);
            return null;
        });

        updater.clear(player);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        if (!config.enabled) {
            return;
        }

        Player player = event.getPlayer();
        String world = player.getWorld().getName();

        if (!config.isWorldEnabled(world)) {
            updater.clear(player);
            return;
        }

        String worldLayout = config.getWorldLayout(world);
        if (worldLayout != null) {
            updater.update(player, worldLayout);
        } else {
            updater.update(player);
        }
    }
}