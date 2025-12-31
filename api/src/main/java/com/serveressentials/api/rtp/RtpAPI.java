package com.serveressentials.api.rtp;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


public interface RtpAPI {

    /**
     * Performs a random teleport for a player in their current world.
     *
     * @param player The player to teleport
     * @return CompletableFuture that completes with true if teleport was successful, false otherwise
     */
    @NotNull CompletableFuture<Boolean> randomTeleport(@NotNull Player player);

    /**
     * Performs a random teleport for a player in a specific world.
     *
     * @param player The player to teleport
     * @param world The target world
     * @return CompletableFuture that completes with true if teleport was successful, false otherwise
     */
    @NotNull CompletableFuture<Boolean> randomTeleport(@NotNull Player player, @NotNull World world);

    /**
     * Opens the RTP GUI for a player.
     *
     * @param player The player to open the GUI for
     * @return CompletableFuture that completes with true if GUI was opened successfully, false otherwise
     */
    @NotNull CompletableFuture<Boolean> openRtpGUI(@NotNull Player player);

    /**
     * Gets the last RTP location for a player.
     *
     * @param playerId The UUID of the player
     * @return CompletableFuture that completes with the last RTP location if present
     */
    @NotNull CompletableFuture<Optional<RtpLocation>> getLastRtpLocation(@NotNull UUID playerId);

    /**
     * Saves an RTP location for a player.
     *
     * @param playerId The UUID of the player
     * @param location The location to save
     * @return CompletableFuture that completes when saving is done
     */
    @NotNull CompletableFuture<Void> saveRtpLocation(@NotNull UUID playerId, @NotNull Location location);

    /**
     * Gets the remaining cooldown time for a player.
     *
     * @param playerId The UUID of the player
     * @return CompletableFuture that completes with remaining cooldown in seconds, or 0 if no cooldown
     */
    @NotNull CompletableFuture<Long> getRemainingCooldown(@NotNull UUID playerId);

    /**
     * Gets RTP configuration for a specific world.
     *
     * @param worldName The name of the world
     * @return CompletableFuture that completes with world configuration, or empty if not configured
     */
    @NotNull CompletableFuture<Optional<RtpWorldConfig>> getWorldConfig(@NotNull String worldName);

    /**
     * Checks if RTP is enabled for a specific world.
     *
     * @param worldName The name of the world
     * @return CompletableFuture that completes with true if enabled, false otherwise
     */
    @NotNull CompletableFuture<Boolean> isRtpEnabled(@NotNull String worldName);

    /**
     * Checks if the RTP feature is enabled globally.
     *
     * @return true if enabled, false otherwise
     */
    boolean isEnabled();

    /**
     * Reloads the RTP configuration.
     *
     * @return CompletableFuture that completes when reload is finished
     */
    @NotNull CompletableFuture<Void> reload();
}