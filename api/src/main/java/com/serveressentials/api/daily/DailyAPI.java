package com.serveressentials.api.daily;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface DailyAPI {

    /**
     * Opens the daily rewards GUI for a player
     * @param player The player
     * @param page The page number to open
     * @return CompletableFuture with true if successful
     */
    @NotNull CompletableFuture<Boolean> openDailyGUI(@NotNull Player player, int page);

    /**
     * Claims a daily reward for a player
     * @param player The player
     * @param day The day number to claim
     * @return CompletableFuture with true if claim was successful
     */
    @NotNull CompletableFuture<Boolean> claimReward(@NotNull Player player, int day);

    /**
     * Gets the last time a player claimed any reward
     * @param playerId The player's UUID
     * @return CompletableFuture with Optional containing the last claim time
     */
    @NotNull CompletableFuture<Optional<LocalDateTime>> getLastClaimTime(@NotNull java.util.UUID playerId);

    /**
     * Gets all days that a player has claimed
     * @param playerId The player's UUID
     * @return CompletableFuture with Set of claimed day numbers
     */
    @NotNull CompletableFuture<Set<Integer>> getClaimedDays(@NotNull java.util.UUID playerId);

    /**
     * Checks if a player is on cooldown
     * @param playerId The player's UUID
     * @return CompletableFuture with true if player cannot claim yet
     */
    @NotNull CompletableFuture<Boolean> hasClaimedToday(@NotNull java.util.UUID playerId);

    /**
     * Gets formatted cooldown information for a player
     * @param playerId The player's UUID
     * @return CompletableFuture with cooldown duration info
     */
    @NotNull CompletableFuture<DailyCooldownInfo> getTimeUntilNextClaim(@NotNull java.util.UUID playerId);

    /**
     * Reloads the daily rewards configuration
     * @return CompletableFuture that completes when reloaded
     */
    @NotNull CompletableFuture<Void> reload();

    /**
     * Checks if the daily rewards feature is enabled
     * @return true if enabled
     */
    boolean isDailyEnabled();
}