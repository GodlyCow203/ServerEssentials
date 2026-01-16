package net.godlycow.org.reports.api;

import com.serveressentials.api.report.ReportAPI;
import com.serveressentials.api.report.ReportInfo;
import com.serveressentials.api.report.ReportStats;
import com.serveressentials.api.report.event.ReportCreateEvent;
import com.serveressentials.api.report.event.ReportClearEvent;
import com.serveressentials.api.report.event.ReportAutoClearEvent;
import net.godlycow.org.EssC;
import net.godlycow.org.commands.impl.ReportCommand;
import net.godlycow.org.reports.ReportConfig;
import net.godlycow.org.reports.model.Report;
import net.godlycow.org.reports.storage.ReportStorage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class ReportAPIImpl implements ReportAPI {
    private final @NotNull EssC plugin;
    private final @NotNull ReportStorage reportStorage;
    private final @NotNull ReportConfig reportConfig;
    private final @NotNull ReportCommand reportCommand;

    public ReportAPIImpl(@NotNull EssC plugin,
                         @NotNull ReportStorage reportStorage,
                         @NotNull ReportConfig reportConfig,
                         @NotNull ReportCommand reportCommand) {
        this.plugin = plugin;
        this.reportStorage = reportStorage;
        this.reportConfig = reportConfig;
        this.reportCommand = reportCommand;
    }

    @Override
    public @NotNull CompletableFuture<Boolean> submitReport(@NotNull Player reporter,
                                                            @NotNull String targetName,
                                                            @NotNull String reason) {
        return CompletableFuture.supplyAsync(() -> {
            long remainingCooldown = getRemainingCooldown(reporter.getUniqueId()).join();
            if (remainingCooldown > 0) {
                return false;
            }

            if (reason.length() > reportConfig.maxReasonLength) {
                return false;
            }

            OfflinePlayer target = plugin.getServer().getOfflinePlayerIfCached(targetName);
            if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
                target = plugin.getServer().getOfflinePlayer(targetName);
                if (!target.hasPlayedBefore() && !target.isOnline()) {
                    return false;
                }
            }

            String reportId = java.util.UUID.randomUUID().toString().substring(0, 8);

            Report report = new Report(
                    reportId,
                    reporter.getUniqueId(),
                    target.getUniqueId(),
                    reason,
                    System.currentTimeMillis(),
                    true
            );

            reportStorage.addReport(report).join();
            reportStorage.saveCooldown(reporter.getUniqueId(), System.currentTimeMillis()).join();

            ReportInfo reportInfo = new ReportInfo(
                    reportId,
                    reporter.getUniqueId(),
                    target.getUniqueId(),
                    reason,
                    System.currentTimeMillis(),
                    true,
                    reporter.getName(),
                    target.getName()
            );

            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.getPluginManager().callEvent(new ReportCreateEvent(reporter, reportInfo));
            });

            return true;
        });
    }

    @Override
    public @NotNull CompletableFuture<Boolean> clearReport(@Nullable Player clearer, @NotNull String reportId) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<Report> reportOpt = reportStorage.getReport(reportId).join();
            if (!reportOpt.isPresent()) {
                return false;
            }

            Report report = reportOpt.get();

            reportStorage.clearReport(reportId).join();

            ReportInfo reportInfo = new ReportInfo(
                    report.id(),
                    report.reporterId(),
                    report.targetId(),
                    report.reason(),
                    report.timestamp(),
                    false,
                    getPlayerName(report.reporterId()),
                    getPlayerName(report.targetId())
            );

            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.getPluginManager().callEvent(new ReportClearEvent(clearer, reportInfo, clearer != null));
            });

            return true;
        });
    }

    @Override
    public @NotNull CompletableFuture<Optional<ReportInfo>> getReport(@NotNull String reportId) {
        return reportStorage.getReport(reportId).thenApply(opt -> opt.map(this::convertToReportInfo));
    }

    @Override
    public @NotNull CompletableFuture<List<ReportInfo>> getAllReports() {
        return reportStorage.getAllReports().thenApply(reports ->
                reports.stream().map(this::convertToReportInfo).collect(Collectors.toList())
        );
    }

    @Override
    public @NotNull CompletableFuture<List<ReportInfo>> getPendingReports() {
        return reportStorage.getPendingReports().thenCompose(reports -> {
            List<ReportInfo> reportInfos = reports.stream().map(this::convertToReportInfo).collect(Collectors.toList());
            return CompletableFuture.completedFuture(reportInfos);
        });
    }

    @Override
    public @NotNull CompletableFuture<ReportStats> getReportStats() {
        return getAllReports().thenApply(allReports -> {
            List<ReportInfo> pending = allReports.stream().filter(ReportInfo::isPending).collect(Collectors.toList());
            List<ReportInfo> cleared = allReports.stream().filter(r -> !r.isPending()).collect(Collectors.toList());

            long oldestTimestamp = allReports.stream()
                    .mapToLong(ReportInfo::getTimestamp)
                    .min()
                    .orElse(0);

            long newestTimestamp = allReports.stream()
                    .mapToLong(ReportInfo::getTimestamp)
                    .max()
                    .orElse(0);

            return new ReportStats(
                    allReports.size(),
                    pending.size(),
                    cleared.size(),
                    oldestTimestamp,
                    newestTimestamp
            );
        });
    }

    @Override
    public @NotNull CompletableFuture<Long> getRemainingCooldown(@NotNull UUID playerId) {
        return reportStorage.getCooldown(playerId).thenApply(lastUsed -> {
            if (lastUsed == 0) {
                return 0L;
            }
            long now = System.currentTimeMillis();
            int cooldownMs = reportConfig.cooldown * 1000;
            long elapsed = now - lastUsed;
            long remaining = cooldownMs - elapsed;
            return Math.max(0, remaining / 1000);
        });
    }

    @Override
    public @NotNull CompletableFuture<Integer> autoClearReports(@Nullable Player clearer) {
        return CompletableFuture.supplyAsync(() -> {
            int autoClearDays = reportConfig.autoClearAfterDays;
            if (autoClearDays <= 0) {
                return 0;
            }

            long cutoffTime = System.currentTimeMillis() - (autoClearDays * 24L * 60 * 60 * 1000);

            return getAllReports().thenCompose(allReports -> {
                List<ReportInfo> toClear = allReports.stream()
                        .filter(report -> report.getTimestamp() < cutoffTime && report.isPending())
                        .collect(Collectors.toList());

                if (toClear.isEmpty()) {
                    return CompletableFuture.completedFuture(0);
                }

                List<CompletableFuture<Boolean>> clearFutures = toClear.stream()
                        .map(report -> clearReport(clearer, report.getId()))
                        .collect(Collectors.toList());

                return CompletableFuture.allOf(clearFutures.toArray(new CompletableFuture[0]))
                        .thenApply(v -> toClear.size());
            }).join();
        }).thenCompose(clearedCount -> {
            if (clearedCount > 0) {
                return getAllReports().thenApply(allReports -> {
                    List<ReportInfo> clearedReports = allReports.stream()
                            .filter(r -> !r.isPending())
                            .collect(Collectors.toList());

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        Bukkit.getPluginManager().callEvent(new ReportAutoClearEvent(clearedReports));
                    });

                    return clearedCount;
                });
            }
            return CompletableFuture.completedFuture(clearedCount);
        });
    }

    @Override
    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("reports.enabled", true);
    }

    @Override
    public @NotNull CompletableFuture<Void> reload() {
        return CompletableFuture.runAsync(() -> {
            plugin.reloadConfig();
            reportConfig.reload();
            plugin.getLogger().info("[ServerEssentials] Report configuration reloaded");
        });
    }

    private @NotNull ReportInfo convertToReportInfo(@NotNull Report report) {
        return new ReportInfo(
                report.id(),
                report.reporterId(),
                report.targetId(),
                report.reason(),
                report.timestamp(),
                report.pending(),
                getPlayerName(report.reporterId()),
                getPlayerName(report.targetId())
        );
    }

    private @Nullable String getPlayerName(@NotNull UUID playerId) {
        OfflinePlayer player = plugin.getServer().getOfflinePlayer(playerId);
        return player.getName();
    }
}