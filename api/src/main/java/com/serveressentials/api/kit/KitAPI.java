package com.serveressentials.api.kit;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public interface KitAPI {

    /**
     * Claims a kit for the specified player.
     *
     * @param player The player claiming the kit
     * @param kitId The ID of the kit to claim
     * @return CompletableFuture that completes with true if claim was successful, false otherwise
     */
    @NotNull CompletableFuture<Boolean> claimKit(@NotNull Player player, @NotNull String kitId);

    /**
     * Opens the kit GUI for the specified player.
     *
     * @param player The player to open the GUI for
     * @return CompletableFuture that completes with true if GUI was opened successfully, false otherwise
     */
    @NotNull CompletableFuture<Boolean> openKitGUI(@NotNull Player player);

    /**
     * Opens the kit preview GUI for the specified kit.
     *
     * @param player The player to open the preview for
     * @param kitId The ID of the kit to preview
     * @return CompletableFuture that completes with true if preview was opened successfully, false otherwise
     */
    @NotNull CompletableFuture<Boolean> openKitPreview(@NotNull Player player, @NotNull String kitId);

    /**
     * Gets a list of all available kits for the specified player.
     *
     * @param player The player to check availability for
     * @return CompletableFuture that completes with a list of kit information
     */
    @NotNull CompletableFuture<List<KitInfo>> getAvailableKits(@NotNull Player player);

    /**
     * Gets the remaining cooldown time for a specific kit.
     *
     * @param player The player to check cooldown for
     * @param kitId The ID of the kit
     * @return CompletableFuture that completes with remaining cooldown in seconds, or 0 if no cooldown
     */
    @NotNull CompletableFuture<Long> getRemainingCooldown(@NotNull Player player, @NotNull String kitId);

    /**
     * Checks if the kit feature is enabled.
     *
     * @return true if enabled, false otherwise
     */
    boolean isEnabled();

    /**
     * Reloads the kit configuration and data.
     *
     * @return CompletableFuture that completes when reload is finished
     */
    @NotNull CompletableFuture<Void> reload();
}