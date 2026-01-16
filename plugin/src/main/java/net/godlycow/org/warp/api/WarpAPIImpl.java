package net.godlycow.org.warp.api;

import com.serveressentials.api.warp.*;
import com.serveressentials.api.warp.event.WarpCreateEvent;
import com.serveressentials.api.warp.event.WarpDeleteEvent;
import com.serveressentials.api.warp.event.WarpTeleportEvent;
import net.godlycow.org.EssC;
import net.godlycow.org.commands.config.WarpConfig;
import net.godlycow.org.warp.WarpManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class WarpAPIImpl implements WarpAPI {
    private final @NotNull EssC plugin;
    private final @NotNull WarpConfig config;
    private final @NotNull WarpManager manager;

    public WarpAPIImpl(@NotNull EssC plugin, @NotNull WarpConfig config, @NotNull WarpManager manager) {
        this.plugin = plugin;
        this.config = config;
        this.manager = manager;
    }

    @Override
    public @NotNull CompletableFuture<Boolean> teleportToWarp(@NotNull Player player, @NotNull String warpName) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<Optional<WarpData>> warpDataOpt = Optional.ofNullable(getWarp(warpName).join());
            if (warpDataOpt.isEmpty()) {
                return false;
            }

            WarpData warpData = warpDataOpt.get().orElse(null);
            if (warpData == null) {
                return false;
            }

            Location location = new Location(
                    Bukkit.getWorld(warpData.getLocation().getWorld()),
                    warpData.getLocation().getX(),
                    warpData.getLocation().getY(),
                    warpData.getLocation().getZ(),
                    warpData.getLocation().getYaw(),
                    warpData.getLocation().getPitch()
            );

            Bukkit.getScheduler().runTask(plugin, () -> {
                player.teleport(location);
                Bukkit.getPluginManager().callEvent(new WarpTeleportEvent(player, warpData));
            });

            return true;
        });
    }

    @Override
    public @NotNull CompletableFuture<Boolean> createWarp(@NotNull String name, @NotNull Location location, @NotNull UUID creator) {
        return CompletableFuture.supplyAsync(() -> {
            WarpLocation warpLocation = new WarpLocation(
                    location.getWorld().getName(),
                    location.getX(),
                    location.getY(),
                    location.getZ(),
                    location.getYaw(),
                    location.getPitch()
            );

            WarpData warpData = new WarpData(name, warpLocation, creator);

            manager.addWarp(name, location, creator).join();

            Player creatorPlayer = Bukkit.getPlayer(creator);
            if (creatorPlayer != null) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Bukkit.getPluginManager().callEvent(new WarpCreateEvent(creatorPlayer, warpData));
                });
            }

            return true;
        });
    }

    @Override
    public @NotNull CompletableFuture<Boolean> deleteWarp(@NotNull String name, @NotNull Player deleter) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<Optional<WarpData>> warpDataOpt = Optional.ofNullable(getWarp(name).join());
            if (warpDataOpt.isEmpty()) {
                return false;
            }

            WarpData warpData = warpDataOpt.get().orElse(null);
            if (warpData == null) {
                return false;
            }

            manager.removeWarp(name).join();

            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.getPluginManager().callEvent(new WarpDeleteEvent(deleter, name, deleter.getUniqueId()));
            });

            return true;
        });
    }

    @Override
    public @NotNull CompletableFuture<Optional<WarpData>> getWarp(@NotNull String name) {
        return manager.getWarp(name).thenApply(locationOpt -> {
            if (locationOpt.isEmpty()) {
                return Optional.empty();
            }

            UUID creator = manager.getCreator(name).join().orElse(UUID.randomUUID());

            return locationOpt.get().map(loc -> {
                WarpLocation warpLocation = new WarpLocation(
                        loc.getWorld().getName(),
                        loc.getX(),
                        loc.getY(),
                        loc.getZ(),
                        loc.getYaw(),
                        loc.getPitch()
                );
                return new WarpData(name, warpLocation, creator);
            });
        });
    }

    @Override
    public @NotNull CompletableFuture<Map<String, WarpLocation>> getAllWarps() {
        return manager.getAllWarps().thenApply(warps -> {
            return warps.entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> {
                        Location loc = entry.getValue();
                        return new WarpLocation(
                                loc.getWorld().getName(),
                                loc.getX(),
                                loc.getY(),
                                loc.getZ(),
                                loc.getYaw(),
                                loc.getPitch()
                        );
                    }
            ));
        });
    }

    @Override
    public @NotNull CompletableFuture<Integer> getWarpCountForPlayer(@NotNull UUID playerId) {
        return manager.getWarpCountByCreator(playerId)
                .thenApply(l -> l.intValue());
    }





    @Override
    public int getMaxWarpsForPlayer(@NotNull Player player) {
        return manager.getMaxWarpsForPlayer(player);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> warpExists(@NotNull String name) {
        return manager.exists(name);
    }

    @Override
    public @NotNull WarpSettings getSettings() {
        return new WarpSettings(config.cooldown, config.defaultMaxWarps);
    }

    @Override
    public boolean isEnabled() {
        return true; // Warp doesn't have an enabled flag in config
    }

    @Override
    public @NotNull CompletableFuture<Void> reload() {
        return CompletableFuture.runAsync(() -> {
            plugin.reloadConfig();
            plugin.getLogger().info("[ServerEssentials] Warp configuration reloaded");
        });
    }
}