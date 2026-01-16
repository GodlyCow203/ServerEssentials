package com.serveressentials.api.report;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


public interface ReportAPI {

    /**
     * Submits a report against a player.
     *
     * @param reporter The player submitting the report
     * @param targetName The name of the player being reported
     * @param reason The reason for the report
     * @return CompletableFuture that completes with true if report was submitted successfully, false otherwise
     */
    @NotNull CompletableFuture<Boolean> submitReport(@NotNull Player reporter, @NotNull String targetName, @NotNull String reason);

    /**
     * Clears a report by ID.
     *
     * @param clearer The player clearing the report (can be null for console/auto-clear)
     * @param reportId The ID of the report to clear
     * @return CompletableFuture that completes with true if report was cleared successfully, false otherwise
     */
    @NotNull CompletableFuture<Boolean> clearReport(@Nullable Player clearer, @NotNull String reportId);

    /**
     * Gets a report by ID.
     *
     * @param reportId The ID of the report
     * @return CompletableFuture that completes with the report if found
     */
    @NotNull CompletableFuture<Optional<ReportInfo>> getReport(@NotNull String reportId);

    /**
     * Gets all reports.
     *
     * @return CompletableFuture that completes with a list of all reports
     */
    @NotNull CompletableFuture<List<ReportInfo>> getAllReports();

    /**
     * Gets all pending reports.
     *
     * @return CompletableFuture that completes with a list of pending reports
     */
    @NotNull CompletableFuture<List<ReportInfo>> getPendingReports();

    /**
     * Gets reports statistics.
     *
     * @return CompletableFuture that completes with report statistics
     */
    @NotNull CompletableFuture<ReportStats> getReportStats();

    /**
     * Gets the remaining cooldown time for a player to submit a report.
     *
     * @param playerId The UUID of the player
     * @return CompletableFuture that completes with remaining cooldown in seconds, or 0 if no cooldown
     */
    @NotNull CompletableFuture<Long> getRemainingCooldown(@NotNull UUID playerId);

    /**
     * Auto-clears reports older than the configured threshold.
     *
     * @param clearer The player triggering auto-clear (can be null for system)
     * @return CompletableFuture that completes with the number of reports auto-cleared
     */
    @NotNull CompletableFuture<Integer> autoClearReports(@Nullable Player clearer);

    /**
     * Checks if the report feature is enabled.
     *
     * @return true if enabled, false otherwise
     */
    boolean isEnabled();

    /**
     * Reloads the report configuration.
     *
     * @return CompletableFuture that completes when reload is finished
     */
    @NotNull CompletableFuture<Void> reload();
}