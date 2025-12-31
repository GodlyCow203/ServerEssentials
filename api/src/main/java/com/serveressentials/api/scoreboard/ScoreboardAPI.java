package com.serveressentials.api.scoreboard;

import com.serveressentials.api.scoreboard.event.ScoreboardReloadEvent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ScoreboardAPI {
    /**
     * Toggles the scoreboard display state for a player
     * @param player The player to toggle scoreboard for
     * @return CompletableFuture with the new enabled state
     */
    @NotNull CompletableFuture<Boolean> toggleScoreboard(@NotNull Player player);

    /**
     * Sets the layout for a player's scoreboard
     * @param player The player to set layout for
     * @param layout The layout name to apply
     * @return CompletableFuture when operation completes
     */
    @NotNull CompletableFuture<Void> setLayout(@NotNull Player player, @NotNull String layout);

    /**
     * Reloads all scoreboard configurations and refreshes active scoreboards
     * @return CompletableFuture when reload completes
     */
    @NotNull CompletableFuture<Void> reload();

    /**
     * Checks if the scoreboard system is enabled
     * @return true if enabled
     */
    boolean isEnabled();

    /**
     * Gets the data for a specific player
     * @param player The player to get data for
     * @return CompletableFuture with player data
     */
    @NotNull CompletableFuture<ScoreboardPlayerData> getPlayerData(@NotNull Player player);

    /**
     * Gets all available layouts
     * @return CompletableFuture with list of layouts
     */
    @NotNull CompletableFuture<List<ScoreboardLayout>> getLayouts();

    /**
     * Gets a specific layout by name
     * @param name The layout name
     * @return CompletableFuture with optional layout
     */
    @NotNull CompletableFuture<java.util.Optional<ScoreboardLayout>> getLayout(@NotNull String name);
}