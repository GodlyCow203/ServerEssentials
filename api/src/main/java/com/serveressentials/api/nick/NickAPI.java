package com.serveressentials.api.nick;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * API interface for nickname management functionality.
 */
public interface NickAPI {

    /**
     * Sets a player's nickname.
     *
     * @param player The player setting the nickname
     * @param nickname The new nickname
     * @return CompletableFuture that completes with true if successful, false otherwise
     */
    @NotNull CompletableFuture<Boolean> setNickname(@NotNull Player player, @NotNull String nickname);

    /**
     * Resets a player's nickname to their original name.
     *
     * @param player The player resetting their nickname
     * @return CompletableFuture that completes with true if successful, false otherwise
     */
    @NotNull CompletableFuture<Boolean> resetNickname(@NotNull Player player);

    /**
     * Resets another player's nickname (admin command).
     *
     * @param sender The admin executing the command
     * @param targetName The target player's name
     * @return CompletableFuture that completes with true if successful, false otherwise
     */
    @NotNull CompletableFuture<Boolean> resetOtherNickname(@NotNull Player sender, @NotNull String targetName);

    /**
     * Gets a player's current nickname.
     *
     * @param playerId The UUID of the player
     * @return CompletableFuture that completes with the nickname if present
     */
    @NotNull CompletableFuture<Optional<NickInfo>> getNickname(@NotNull UUID playerId);

    /**
     * Gets all cached nicknames (for validation).
     *
     * @return List of NickInfo objects
     */
    @NotNull List<NickInfo> getAllNicknames();

    /**
     * Gets validation rules for nicknames.
     *
     * @return NickValidationRules object
     */
    @NotNull NickValidationRules getValidationRules();

    /**
     * Validates a nickname against all rules.
     *
     * @param player The player setting the nickname
     * @param nickname The nickname to validate
     * @return CompletableFuture that completes with true if valid, false otherwise
     */
    @NotNull CompletableFuture<Boolean> validateNickname(@NotNull Player player, @NotNull String nickname);

    /**
     * Gets the remaining cooldown time for a player.
     *
     * @param playerId The UUID of the player
     * @return CompletableFuture that completes with remaining cooldown in seconds, or 0 if no cooldown
     */
    @NotNull CompletableFuture<Long> getRemainingCooldown(@NotNull UUID playerId);

    /**
     * Gets the number of nickname changes a player has made today.
     *
     * @param playerId The UUID of the player
     * @return CompletableFuture that completes with the number of changes
     */
    @NotNull CompletableFuture<Integer> getDailyChanges(@NotNull UUID playerId);

    /**
     * Checks if the nickname feature is enabled.
     *
     * @return true if enabled, false otherwise
     */
    boolean isEnabled();

    /**
     * Reloads the nickname configuration and refreshes all nicknames.
     *
     * @return CompletableFuture that completes when reload is finished
     */
    @NotNull CompletableFuture<Void> reload();
}