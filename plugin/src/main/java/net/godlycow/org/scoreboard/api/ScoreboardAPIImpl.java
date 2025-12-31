package net.godlycow.org.scoreboard.api;

import com.serveressentials.api.scoreboard.ScoreboardAPI;
import com.serveressentials.api.scoreboard.ScoreboardLayout;
import com.serveressentials.api.scoreboard.ScoreboardPlayerData;
import com.serveressentials.api.scoreboard.ScoreboardWorldSetting;
import com.serveressentials.api.scoreboard.event.ScoreboardLayoutChangeEvent;
import com.serveressentials.api.scoreboard.event.ScoreboardReloadEvent;
import com.serveressentials.api.scoreboard.event.ScoreboardToggleEvent;
import net.godlycow.org.ServerEssentials;
import net.godlycow.org.commands.config.ScoreboardConfig;
import net.godlycow.org.scoreboard.runtime.ScoreboardUpdater;
import net.godlycow.org.scoreboard.storage.ScoreboardStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class ScoreboardAPIImpl implements ScoreboardAPI {
    private final @NotNull ServerEssentials plugin;
    private final @NotNull ScoreboardConfig config;
    private final @NotNull ScoreboardStorage storage;
    private final @NotNull ScoreboardUpdater updater;

    public ScoreboardAPIImpl(@NotNull ServerEssentials plugin, @NotNull ScoreboardConfig config,
                             @NotNull ScoreboardStorage storage, @NotNull ScoreboardUpdater updater) {
        this.plugin = plugin;
        this.config = config;
        this.storage = storage;
        this.updater = updater;
    }

    @Override
    public @NotNull CompletableFuture<Boolean> toggleScoreboard(@NotNull Player player) {
        return CompletableFuture.supplyAsync(() -> {
            UUID uuid = player.getUniqueId();
            boolean current = storage.isEnabled(uuid);
            boolean newState = !current;

            storage.setEnabled(uuid, newState).join();

            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.getPluginManager().callEvent(new ScoreboardToggleEvent(player, newState));
            });

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (newState) {
                    updater.update(player);
                } else {
                    updater.clear(player);
                }
            });

            return newState;
        });
    }

    @Override
    public @NotNull CompletableFuture<Void> setLayout(@NotNull Player player, @NotNull String layout) {
        return CompletableFuture.runAsync(() -> {
            if (!config.layoutExists(layout)) {
                throw new IllegalArgumentException("Layout '" + layout + "' does not exist");
            }

            UUID uuid = player.getUniqueId();
            String previousLayout = storage.getLayout(uuid);

            storage.setLayout(uuid, layout).join();

            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.getPluginManager().callEvent(new ScoreboardLayoutChangeEvent(player, layout, previousLayout));
            });

            Bukkit.getScheduler().runTask(plugin, () -> updater.update(player, layout));
        });
    }

    @Override
    public @NotNull CompletableFuture<Void> reload() {
        return CompletableFuture.runAsync(() -> {
            plugin.reloadConfig();
            Bukkit.getOnlinePlayers().forEach(updater::updateAsync);

            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    Bukkit.getPluginManager().callEvent(new ScoreboardReloadEvent(player));
                });
            });
        });
    }

    @Override
    public boolean isEnabled() {
        return config.enabled;
    }

    @Override
    public @NotNull CompletableFuture<com.serveressentials.api.scoreboard.ScoreboardPlayerData> getPlayerData(@NotNull Player player) {
        return storage.loadPlayer(player.getUniqueId())
                .thenApply(storageData -> new com.serveressentials.api.scoreboard.ScoreboardPlayerData(
                        storageData.enabled(),
                        storageData.layout(),
                        storageData.lastUpdate(),
                        storageData.joinTime()
                ));
    }


    @Override
    public @NotNull CompletableFuture<List<ScoreboardLayout>> getLayouts() {
        return CompletableFuture.supplyAsync(() ->
                config.getLayoutNames().stream()
                        .map(config::getLayout)
                        .map(layout -> new ScoreboardLayout(layout.title(), layout.lines(), layout.maxLines()))
                        .collect(Collectors.toList())
        );
    }

    @Override
    public @NotNull CompletableFuture<Optional<ScoreboardLayout>> getLayout(@NotNull String name) {
        return CompletableFuture.supplyAsync(() -> {
            if (!config.layoutExists(name)) {
                return Optional.empty();
            }
            ScoreboardConfig.Layout layout = config.getLayout(name);
            return Optional.of(new ScoreboardLayout(layout.title(), layout.lines(), layout.maxLines()));
        });
    }
}