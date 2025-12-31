package com.serveressentials.api.warp;

import com.serveressentials.api.warp.event.WarpTeleportEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface WarpAPI {
    /**
     * Teleports a player to a warp
     * @param player The player to teleport
     * @param warpName The name of the warp
     * @return CompletableFuture with success status
     */
    @NotNull CompletableFuture<Boolean> teleportToWarp(@NotNull Player player, @NotNull String warpName);

    /**
     * Creates a new warp
     * @param name The warp name
     * @param location The warp location
     * @param creator The creator's UUID
     * @return CompletableFuture with success status
     */
    @NotNull CompletableFuture<Boolean> createWarp(@NotNull String name, @NotNull Location location, @NotNull UUID creator);

    /**
     * Deletes a warp
     * @param name The warp name
     * @param deleter The player attempting deletion (for permission checks)
     * @return CompletableFuture with success status
     */
    @NotNull CompletableFuture<Boolean> deleteWarp(@NotNull String name, @NotNull Player deleter);

    /**
     * Gets a warp by name
     * @param name The warp name
     * @return CompletableFuture with optional warp data
     */
    @NotNull CompletableFuture<java.util.Optional<WarpData>> getWarp(@NotNull String name);

    /**
     * Gets all warps
     * @return CompletableFuture with map of warp names to locations
     */
    @NotNull CompletableFuture<Map<String, WarpLocation>> getAllWarps();

    /**
     * Gets warp count for a specific player
     * @param playerId The player's UUID
     * @return CompletableFuture with warp count
     */
    @NotNull CompletableFuture<Integer> getWarpCountForPlayer(@NotNull UUID playerId);

    /**
     * Gets the maximum warps allowed for a player
     * @param player The player
     * @return The maximum warp count
     */
    int getMaxWarpsForPlayer(@NotNull Player player);

    /**
     * Checks if a warp exists
     * @param name The warp name
     * @return CompletableFuture with existence status
     */
    @NotNull CompletableFuture<Boolean> warpExists(@NotNull String name);

    /**
     * Gets warp settings
     * @return The warp settings
     */
    @NotNull WarpSettings getSettings();

    /**
     * Checks if warp system is enabled
     * @return true if enabled
     */
    boolean isEnabled();

    /**
     * Reloads warp configuration
     * @return CompletableFuture when reload completes
     */
    @NotNull CompletableFuture<Void> reload();
}