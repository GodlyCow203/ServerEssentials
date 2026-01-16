package com.serveressentials.api.back;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * API for managing player back locations and teleportation
 */
public interface BackAPI {

    /**
     * Teleports a player to their last saved location
     * @param player The player to teleport
     * @return CompletableFuture with true if successful, false otherwise
     */
    @NotNull
    CompletableFuture<Boolean> teleportBack(@NotNull Player player);

    /**
     * Teleports a player to the lobby location
     * @param player The player to teleport
     * @return CompletableFuture with true if successful, false otherwise
     */
    @NotNull
    CompletableFuture<Boolean> teleportToLobby(@NotNull Player player);

    /**
     * Teleports a player to their last death location
     * @param player The player to teleport
     * @return CompletableFuture with true if successful, false otherwise
     */
    @NotNull
    CompletableFuture<Boolean> teleportToDeath(@NotNull Player player);

    /**
     * Saves a location as the player's back location
     * @param player The player
     * @param location The location to save
     * @return CompletableFuture that completes when saved
     */
    @NotNull
    CompletableFuture<Void> setBackLocation(@NotNull Player player, @NotNull Location location);

    /**
     * Gets the player's saved back location
     * @param player The player
     * @return CompletableFuture with Optional containing the location if present
     */
    @NotNull
    CompletableFuture<Optional<Location>> getBackLocation(@NotNull Player player);

    /**
     * Checks if a player has a saved back location
     * @param player The player
     * @return CompletableFuture with true if they have a back location
     */
    @NotNull
    CompletableFuture<Boolean> hasBackLocation(@NotNull Player player);

    /**
     * Clears a player's saved back location
     * @param player The player
     * @return CompletableFuture that completes when cleared
     */
    @NotNull
    CompletableFuture<Void> clearBackLocation(@NotNull Player player);

    /**
     * Checks if the back feature is enabled
     * @return true if enabled
     */
    boolean isBackEnabled();

    /**
     * Reloads the back system configuration
     * @return CompletableFuture that completes when reloaded
     */
    @NotNull
    CompletableFuture<Void> reload();
}