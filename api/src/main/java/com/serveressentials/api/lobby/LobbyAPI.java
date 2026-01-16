package com.serveressentials.api.lobby;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * API interface for lobby management functionality.
 */
public interface LobbyAPI {

    /**
     * Teleports a player to the lobby (global or their current world if per-world is enabled).
     *
     * @param player The player to teleport
     * @return CompletableFuture that completes with true if teleport was successful, false otherwise
     */
    @NotNull CompletableFuture<Boolean> teleportToLobby(@NotNull Player player);

    /**
     * Teleports a player to a specific lobby (world-specific or global fallback).
     *
     * @param player The player to teleport
     * @param worldKey The world key for per-world lobby, or null for global
     * @return CompletableFuture that completes with true if teleport was successful, false otherwise
     */
    @NotNull CompletableFuture<Boolean> teleportToLobby(@NotNull Player player, @Nullable String worldKey);

    /**
     * Sets the global lobby location.
     *
     * @param player The player setting the lobby
     * @param location The lobby location
     * @return CompletableFuture that completes when the lobby is set
     */
    @NotNull CompletableFuture<Void> setLobby(@NotNull Player player, @NotNull Location location);

    /**
     * Sets a lobby location (global or world-specific).
     *
     * @param player The player setting the lobby
     * @param location The lobby location
     * @param worldKey The world key for per-world lobby, or null for global
     * @return CompletableFuture that completes when the lobby is set
     */
    @NotNull CompletableFuture<Void> setLobby(@NotNull Player player, @NotNull Location location, @Nullable String worldKey);

    /**
     * Removes the global lobby.
     *
     * @return CompletableFuture that completes when the lobby is removed
     */
    @NotNull CompletableFuture<Void> removeLobby();

    /**
     * Removes a lobby (global or world-specific).
     *
     * @param worldKey The world key for per-world lobby, or null for global
     * @return CompletableFuture that completes when the lobby is removed
     */
    @NotNull CompletableFuture<Void> removeLobby(@Nullable String worldKey);

    /**
     * Gets a lobby location.
     *
     * @param worldKey The world key for per-world lobby, or null for global
     * @return CompletableFuture that completes with the lobby location if present
     */
    @NotNull CompletableFuture<Optional<LobbyLocation>> getLobby(@Nullable String worldKey);

    /**
     * Checks if a lobby exists.
     *
     * @param worldKey The world key for per-world lobby, or null for global
     * @return CompletableFuture that completes with true if lobby exists, false otherwise
     */
    @NotNull CompletableFuture<Boolean> hasLobby(@Nullable String worldKey);

    /**
     * Checks if the lobby feature is enabled.
     *
     * @return true if enabled, false otherwise
     */
    boolean isEnabled();

    /**
     * Reloads the lobby configuration.
     *
     * @return CompletableFuture that completes when reload is finished
     */
    @NotNull CompletableFuture<Void> reload();
}