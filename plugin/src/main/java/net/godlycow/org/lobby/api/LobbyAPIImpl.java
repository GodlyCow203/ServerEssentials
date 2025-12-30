package net.godlycow.org.lobby.api;

import com.serveressentials.api.lobby.LobbyAPI;
import com.serveressentials.api.lobby.LobbyLocation;
import com.serveressentials.api.lobby.AnimationOptions;
import com.serveressentials.api.lobby.event.LobbyTeleportEvent;
import com.serveressentials.api.lobby.event.LobbySetEvent;
import com.serveressentials.api.lobby.event.LobbyRemoveEvent;
import net.godlycow.org.ServerEssentials;
import net.godlycow.org.commands.config.LobbyConfig;
import net.godlycow.org.lobby.helper.AnimationHelper;
import net.godlycow.org.lobby.storage.LobbyStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class LobbyAPIImpl implements LobbyAPI {
    private final @NotNull ServerEssentials plugin;
    private final @NotNull LobbyStorage lobbyStorage;
    private final @NotNull LobbyConfig lobbyConfig;

    public LobbyAPIImpl(@NotNull ServerEssentials plugin,
                        @NotNull LobbyStorage lobbyStorage,
                        @NotNull LobbyConfig lobbyConfig) {
        this.plugin = plugin;
        this.lobbyStorage = lobbyStorage;
        this.lobbyConfig = lobbyConfig;
    }

    @Override
    public @NotNull CompletableFuture<Boolean> teleportToLobby(@NotNull Player player) {
        String worldKey = lobbyConfig.isPerWorld() ? player.getWorld().getName() : null;
        return teleportToLobby(player, worldKey);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> teleportToLobby(@NotNull Player player, @Nullable String worldKey) {
        return CompletableFuture.supplyAsync(() -> {
            if (!player.hasPermission("serveressentials.command.lobby")) {
                return false;
            }

            if (!lobbyStorage.hasLobby(worldKey).join()) {
                return false;
            }

            Optional<Location> locationOpt = lobbyStorage.getLobby(worldKey).join();
            if (!locationOpt.isPresent()) {
                return false;
            }

            Location bukkitLocation = locationOpt.get();

            LobbyLocation lobbyLocation = new LobbyLocation(
                    bukkitLocation.getWorld().getName(),
                    bukkitLocation.getX(), bukkitLocation.getY(), bukkitLocation.getZ(),
                    bukkitLocation.getYaw(), bukkitLocation.getPitch()
            );

            Location fromLocation = player.getLocation();

            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.getPluginManager().callEvent(
                        new LobbyTeleportEvent(player, lobbyLocation, fromLocation, worldKey, worldKey != null)
                );

                if (lobbyConfig.isAnimationEnabled() && lobbyConfig.getAnimation() != null) {
                    AnimationHelper.playTeleportAnimation(plugin, player, lobbyConfig.getAnimation());
                }

                player.teleport(bukkitLocation);

                player.sendMessage(org.bukkit.ChatColor.GREEN + "Teleported to lobby!");
            });

            return true;
        });
    }

    @Override
    public @NotNull CompletableFuture<Void> setLobby(@NotNull Player player, @NotNull Location location) {
        return setLobby(player, location, null);
    }

    @Override
    public @NotNull CompletableFuture<Void> setLobby(@NotNull Player player, @NotNull Location location, @Nullable String worldKey) {
        return CompletableFuture.runAsync(() -> {
            if (worldKey == null && !player.hasPermission("serveressentials.command.lobby.set")) {
                throw new SecurityException("No permission to set global lobby");
            }
            if (worldKey != null && !player.hasPermission("serveressentials.command.lobby.world")) {
                throw new SecurityException("No permission to set world lobby");
            }

            LobbyLocation lobbyLocation = new LobbyLocation(
                    location.getWorld().getName(),
                    location.getX(), location.getY(), location.getZ(),
                    location.getYaw(), location.getPitch()
            );

            if (worldKey == null) {
                lobbyStorage.setLobby(location).join();
            } else {
                lobbyStorage.setWorldLobby(worldKey, location).join();
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.getPluginManager().callEvent(
                        new LobbySetEvent(player, lobbyLocation, worldKey, worldKey != null)
                );
            });
        });
    }

    @Override
    public @NotNull CompletableFuture<Void> removeLobby() {
        return removeLobby(null);
    }

    @Override
    public @NotNull CompletableFuture<Void> removeLobby(@Nullable String worldKey) {
        return CompletableFuture.runAsync(() -> {
            lobbyStorage.removeLobby(worldKey).join();
        });
    }

    @Override
    public @NotNull CompletableFuture<Optional<LobbyLocation>> getLobby(@Nullable String worldKey) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<Location> locationOpt = lobbyStorage.getLobby(worldKey).join();
            return locationOpt.map(location -> new LobbyLocation(
                    location.getWorld().getName(),
                    location.getX(), location.getY(), location.getZ(),
                    location.getYaw(), location.getPitch()
            ));
        });
    }

    @Override
    public @NotNull CompletableFuture<Boolean> hasLobby(@Nullable String worldKey) {
        return lobbyStorage.hasLobby(worldKey);
    }

    @Override
    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("lobby.enabled", true);
    }

    @Override
    public @NotNull CompletableFuture<Void> reload() {
        return CompletableFuture.runAsync(() -> {
            plugin.reloadConfig();
            lobbyConfig.reload();
            plugin.getLogger().info("[ServerEssentials] Lobby configuration reloaded");
        });
    }
}