package net.godlycow.org.back.api;

import com.serveressentials.api.back.BackAPI;
import com.serveressentials.api.back.UsageTypes;
import com.serveressentials.api.back.event.BackEvent;
import com.serveressentials.api.back.event.BackLocationSaveEvent;
import com.serveressentials.api.back.event.BackTeleportEvent;
import net.godlycow.org.EssC;
import net.godlycow.org.back.BackManager;
import net.godlycow.org.back.storage.BackDataStorage;
import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.commands.config.BackConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class BackAPIImpl implements BackAPI {
    private static final String PERMISSION_BACK = "essentialsc.command.back";
    private static final String PERMISSION_LOBBY = "essentialsc.command.back.lobby";
    private static final String PERMISSION_DEATH = "essentialsc.command.back.death";

    private final @NotNull EssC plugin;
    private final @NotNull BackManager backManager;
    private final @NotNull BackConfig config;
    private final @NotNull BackDataStorage storage;
    private final @NotNull PlayerLanguageManager langManager;
    private final @NotNull CommandDataStorage dataStorage;

    public BackAPIImpl(@NotNull EssC plugin,
                       @NotNull BackManager backManager,
                       @NotNull BackConfig config,
                       @NotNull BackDataStorage storage,
                       @NotNull PlayerLanguageManager langManager,
                       @NotNull CommandDataStorage dataStorage) {
        this.plugin = plugin;
        this.backManager = backManager;
        this.config = config;
        this.storage = storage;
        this.langManager = langManager;
        this.dataStorage = dataStorage;
    }

    @Override
    public @NotNull CompletableFuture<Boolean> teleportBack(@NotNull Player player) {
        return backManager.getLastLocation(player.getUniqueId()).thenCompose(location -> {
            if (location == null) {
                sendMessage(player, "commands.back.no-back", "<red>No previous location saved.");
                return CompletableFuture.completedFuture(false);
            }

            return performTeleport(player, location, BackEvent.BackType.BACK_LOCATION)
                    .thenApply(success -> {
                        if (success) {
                            backManager.clearBack(player.getUniqueId());
                            trackUsage(player.getUniqueId(), UsageTypes.BACK);
                        }
                        return success;
                    });
        });
    }

    @Override
    public @NotNull CompletableFuture<Boolean> teleportToLobby(@NotNull Player player) {
        if (!player.hasPermission(PERMISSION_LOBBY)) {
            sendNoPermissionMessage(player, PERMISSION_LOBBY);
            return CompletableFuture.completedFuture(false);
        }

        Location lobbyLocation = getLobbyLocation();
        if (lobbyLocation == null) {
            sendMessage(player, "commands.back.lobby-not-set", "<red>Lobby location not set or world is unloaded.");
            return CompletableFuture.completedFuture(false);
        }

        return performTeleport(player, lobbyLocation, BackEvent.BackType.LOBBY)
                .thenApply(success -> {
                    if (success) {
                        trackUsage(player.getUniqueId(), UsageTypes.LOBBY);
                    }
                    return success;
                });
    }

    @Override
    public @NotNull CompletableFuture<Boolean> teleportToDeath(@NotNull Player player) {
        if (!player.hasPermission(PERMISSION_DEATH)) {
            sendNoPermissionMessage(player, PERMISSION_DEATH);
            return CompletableFuture.completedFuture(false);
        }

        Location deathLocation = player.getLastDeathLocation();
        if (deathLocation == null) {
            sendMessage(player, "commands.back.no-death-location", "<red>No death location found.");
            return CompletableFuture.completedFuture(false);
        }

        return performTeleport(player, deathLocation, BackEvent.BackType.DEATH_LOCATION)
                .thenApply(success -> {
                    if (success) {
                        trackUsage(player.getUniqueId(), UsageTypes.DEATH);
                    }
                    return success;
                });
    }

    @Override
    public @NotNull CompletableFuture<Void> setBackLocation(@NotNull Player player, @NotNull Location location) {
        return backManager.setLastLocation(player.getUniqueId(), location)
                .thenRun(() -> {
                    Bukkit.getPluginManager().callEvent(new BackLocationSaveEvent(player, location));
                });
    }

    @Override
    public @NotNull CompletableFuture<Optional<Location>> getBackLocation(@NotNull Player player) {
        return backManager.getLastLocation(player.getUniqueId())
                .thenApply(location -> Optional.ofNullable(location));
    }

    @Override
    public @NotNull CompletableFuture<Boolean> hasBackLocation(@NotNull Player player) {
        return backManager.hasBack(player.getUniqueId());
    }

    @Override
    public @NotNull CompletableFuture<Void> clearBackLocation(@NotNull Player player) {
        return backManager.clearBack(player.getUniqueId());
    }

    @Override
    public boolean isBackEnabled() {
        return plugin.getConfig().getBoolean("back.enabled", true);
    }

    @Override
    public @NotNull CompletableFuture<Void> reload() {
        return CompletableFuture.runAsync(() -> {
            plugin.reloadConfig();
            Bukkit.getLogger().info("[ServerEssentials] Back configuration reloaded");
        });
    }

    private @NotNull CompletableFuture<Boolean> performTeleport(@NotNull Player player,
                                                                @NotNull Location destination,
                                                                @NotNull BackEvent.BackType backType) {
        Location from = player.getLocation();

        CompletableFuture<Boolean> result = new CompletableFuture<>();

        Bukkit.getScheduler().runTask(plugin, () -> {
            player.teleport(destination);
            sendMessage(player, "commands.back.teleported-" + backType.name().toLowerCase(),
                    "<green>Teleported successfully.");

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                Bukkit.getPluginManager().callEvent(new BackTeleportEvent(player, backType, from, destination));
                result.complete(true);
            });
        });

        return result;
    }

    private @Nullable Location getLobbyLocation() {
        org.bukkit.World world = Bukkit.getWorld(config.lobbyWorld());
        if (world == null) return null;

        return new Location(world, config.lobbyX(), config.lobbyY(), config.lobbyZ(),
                config.lobbyYaw(), config.lobbyPitch());
    }

    private void sendMessage(@NotNull Player player, @NotNull String key, @NotNull String defaultMsg) {
        player.sendMessage(langManager.getMessageFor(player, key, defaultMsg).toString());
    }

    private void sendNoPermissionMessage(@NotNull Player player, @NotNull String permission) {
        player.sendMessage(langManager.getMessageFor(player, "commands.back.no-permission-sub",
                "<red>You need permission <yellow>{subpermission}</yellow>!",
                net.godlycow.org.language.LanguageManager.ComponentPlaceholder.of("{subpermission}", permission)));
    }

    private void trackUsage(@NotNull UUID playerId, @NotNull String type) {
        dataStorage.getState(playerId, "back", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "back", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "back", "last_type", type);
            dataStorage.setState(playerId, "back", "last_used", String.valueOf(System.currentTimeMillis()));
        });
    }


}