package com.godlycow.testapi;

import com.serveressentials.api.report.ReportAPI;
import com.serveressentials.api.report.ReportInfo;
import com.serveressentials.api.report.ReportStats;
import com.serveressentials.api.report.event.ReportCreateEvent;
import com.serveressentials.api.report.event.ReportClearEvent;
import com.serveressentials.api.report.event.ReportAutoClearEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;


public final class ReportAPITestCommand implements CommandExecutor, Listener {
    private final JavaPlugin plugin;
    private ReportAPI api;

    public ReportAPITestCommand(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void setAPI(@NotNull ReportAPI api) {
        this.api = api;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        if (api == null) {
            player.sendMessage("§cReportAPI is not yet available. Please try again in a moment.");
            return true;
        }

        if (args.length >= 3 && args[0].equalsIgnoreCase("submit")) {
            String targetName = args[1];
            String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

            api.submitReport(player, targetName, reason).thenAccept(success -> {
                if (success) {
                    player.sendMessage("§aReport submitted against " + targetName + "!");
                } else {
                    player.sendMessage("§cFailed to submit report! Target not found, on cooldown, or reason too long.");
                }
            });
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("clear")) {
            String reportId = args[1];
            api.clearReport(player, reportId).thenAccept(success -> {
                if (success) {
                    player.sendMessage("§aReport " + reportId + " cleared!");
                } else {
                    player.sendMessage("§cReport not found or failed to clear!");
                }
            });
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("get")) {
            String reportId = args[1];
            api.getReport(reportId).thenAccept(opt -> {
                if (opt.isPresent()) {
                    ReportInfo report = opt.get();
                    player.sendMessage("§6Report " + reportId + ":");
                    player.sendMessage("§7Reporter: " + report.getReporterName());
                    player.sendMessage("§7Target: " + report.getTargetName());
                    player.sendMessage("§7Reason: " + report.getReason());
                    player.sendMessage("§7Pending: " + report.isPending());
                    player.sendMessage("§7Time: " + new java.util.Date(report.getTimestamp()));
                } else {
                    player.sendMessage("§cReport not found!");
                }
            });
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("all")) {
            api.getAllReports().thenAccept(reports -> {
                if (reports.isEmpty()) {
                    player.sendMessage("§eNo reports found.");
                } else {
                    player.sendMessage("§6All Reports (" + reports.size() + "):");
                    for (ReportInfo report : reports) {
                        player.sendMessage("§7[" + report.getId() + "] " +
                                report.getReporterName() + " → " +
                                report.getTargetName() + ": " +
                                report.getReason() + " (" +
                                (report.isPending() ? "Pending" : "Cleared") + ")");
                    }
                }
            });
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("pending")) {
            api.getPendingReports().thenAccept(reports -> {
                if (reports.isEmpty()) {
                    player.sendMessage("§eNo pending reports.");
                } else {
                    player.sendMessage("§6Pending Reports (" + reports.size() + "):");
                    for (ReportInfo report : reports) {
                        player.sendMessage("§7[" + report.getId() + "] " +
                                report.getReporterName() + " → " +
                                report.getTargetName() + ": " + report.getReason());
                    }
                }
            });
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("stats")) {
            api.getReportStats().thenAccept(stats -> {
                player.sendMessage("§6Report Statistics:");
                player.sendMessage("§7Total: " + stats.getTotalReports());
                player.sendMessage("§7Pending: " + stats.getPendingReports());
                player.sendMessage("§7Cleared: " + stats.getClearedReports());
                if (stats.getOldestReportTimestamp() > 0) {
                    player.sendMessage("§7Oldest: " + new java.util.Date(stats.getOldestReportTimestamp()));
                }
                if (stats.getNewestReportTimestamp() > 0) {
                    player.sendMessage("§7Newest: " + new java.util.Date(stats.getNewestReportTimestamp()));
                }
            });
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("cooldown")) {
            api.getRemainingCooldown(player.getUniqueId()).thenAccept(remaining -> {
                if (remaining > 0) {
                    player.sendMessage("§eRemaining cooldown: " + remaining + " seconds");
                } else {
                    player.sendMessage("§aNo cooldown active");
                }
            });
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("autoclear")) {
            player.sendMessage("§eRunning auto-clear...");
            api.autoClearReports(player).thenAccept(count -> {
                player.sendMessage("§aAuto-cleared " + count + " reports!");
            });
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("status")) {
            player.sendMessage("§6ReportAPI Status:");
            player.sendMessage("§7Enabled: " + api.isEnabled());
            api.getReportStats().thenAccept(stats -> {
                player.sendMessage("§7Total Reports: " + stats.getTotalReports());
            });
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            api.reload().thenAccept(v -> {
                player.sendMessage("§aReport configuration reloaded!");
            });
            return true;
        }

        sendUsage(player);
        return true;
    }

    @EventHandler
    public void onReportCreate(@NotNull ReportCreateEvent event) {
        plugin.getLogger().info("[ReportAPITest] " + event.getPlayer().getName() +
                " reported " + event.getReportInfo().getTargetName() +
                ": " + event.getReportInfo().getReason());
    }

    @EventHandler
    public void onReportClear(@NotNull ReportClearEvent event) {
        String clearerName = event.getPlayer() != null ? event.getPlayer().getName() : "System";
        plugin.getLogger().info("[ReportAPITest] Report " + event.getReportInfo().getId() +
                " cleared by " + clearerName +
                " (Manual: " + event.isManualClear() + ")");
    }

    @EventHandler
    public void onReportAutoClear(@NotNull ReportAutoClearEvent event) {
        plugin.getLogger().info("[ReportAPITest] Auto-cleared " + event.getClearedReports().size() + " reports");
    }

    private void sendUsage(@NotNull Player player) {
        player.sendMessage("§6ReportAPI Test Command Usage:");
        player.sendMessage("§7/reportapitest submit <player> <reason...> - Submit a report");
        player.sendMessage("§7/reportapitest clear <id> - Clear a report");
        player.sendMessage("§7/reportapitest get <id> - Get report details");
        player.sendMessage("§7/reportapitest all - List all reports");
        player.sendMessage("§7/reportapitest pending - List pending reports");
        player.sendMessage("§7/reportapitest stats - Show statistics");
        player.sendMessage("§7/reportapitest cooldown - Check your cooldown");
        player.sendMessage("§7/reportapitest autoclear - Auto-clear old reports");
        player.sendMessage("§7/reportapitest status - Show API status");
        player.sendMessage("§7/reportapitest reload - Reload configuration");
    }
}