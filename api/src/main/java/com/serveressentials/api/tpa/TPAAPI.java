package com.serveressentials.api.tpa;

import com.serveressentials.api.tpa.event.TPARequestSendEvent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface TPAAPI {
    /**
     * Sends a TPA request from sender to target
     * @param sender The player sending the request
     * @param target The target player
     * @param here Whether this is a TPAHere request
     * @return CompletableFuture with request data
     */
    @NotNull CompletableFuture<TPARequestData> sendRequest(@NotNull Player sender, @NotNull Player target, boolean here);

    /**
     * Accepts a TPA request
     * @param target The player accepting the request
     * @param senderId The UUID of the request sender (or null for most recent)
     * @return CompletableFuture with accept result
     */
    @NotNull CompletableFuture<Boolean> acceptRequest(@NotNull Player target, @Nullable UUID senderId);

    /**
     * Denies a TPA request
     * @param target The player denying the request
     * @param senderId The UUID of the request sender (or null for most recent)
     * @return CompletableFuture with deny result
     */
    @NotNull CompletableFuture<Boolean> denyRequest(@NotNull Player target, @Nullable UUID senderId);

    /**
     * Cancels a TPA request
     * @param sender The player canceling the request
     * @param targetId The UUID of the request target (or null to cancel all)
     * @return CompletableFuture with cancel result
     */
    @NotNull CompletableFuture<Integer> cancelRequest(@NotNull Player sender, @Nullable UUID targetId);

    /**
     * Sends a TPA request to all online players
     * @param sender The player sending the request
     * @return CompletableFuture with count of requests sent
     */
    @NotNull CompletableFuture<Integer> sendRequestToAll(@NotNull Player sender);

    /**
     * Toggles TPA requests for a player
     * @param player The player toggling their status
     * @return CompletableFuture with new toggle state
     */
    @NotNull CompletableFuture<Boolean> toggleRequests(@NotNull Player player);

    /**
     * Gets TPA data for a player
     * @param playerId The player's UUID
     * @return CompletableFuture with player data
     */
    @NotNull CompletableFuture<TPAPlayerData> getPlayerData(@NotNull UUID playerId);

    /**
     * Gets all active TPA requests for a target player
     * @param targetId The target player's UUID
     * @return CompletableFuture with list of requests
     */
    @NotNull CompletableFuture<List<TPARequestData>> getActiveRequests(@NotNull UUID targetId);

    /**
     * Gets TPA settings
     * @return The settings
     */
    @NotNull TPASettings getSettings();

    /**
     * Gets TPA cost configuration
     * @return The cost configuration
     */
    @NotNull TPACosts getCosts();

    /**
     * Checks if TPA is enabled
     * @return true if enabled
     */
    boolean isEnabled();

    /**
     * Reloads TPA configuration
     * @return CompletableFuture when reload completes
     */
    @NotNull CompletableFuture<Void> reload();
}