package net.lunark.io.reports;

import net.kyori.adventure.text.Component;
import net.lunark.io.language.LanguageManager;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ReportCommand implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;
    private final PlayerLanguageManager langManager;
    private final ReportStorage storage;
    private final ReportConfig config;

    public ReportCommand(JavaPlugin plugin, PlayerLanguageManager langManager, ReportStorage storage, ReportConfig config) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.storage = storage;
        this.config = config;
    }

    private Player getPlayerOrNull(CommandSender sender) {
        return sender instanceof Player ? (Player) sender : null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "report":
                return handleReport(sender, args);
            case "reportclear":
                return handleClear(sender, args);
            default:
                return false;
        }
    }

    private boolean handleReport(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            Component msg = langManager.getMessageFor(null, "commands.only-player",
                    "<red>Only players can use this command.");
            sender.sendMessage(msg.toString());
            return true;
        }

        if (!player.hasPermission("serveressentials.command.report")) {
            Component msg = langManager.getMessageFor(player, "commands.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    LanguageManager.ComponentPlaceholder.of("{permission}", "serveressentials.command.report"));
            player.sendMessage(msg);
            return true;
        }

        if (args.length < 2) {
            Component msg = langManager.getMessageFor(player, "reports.command.usage",
                    "<red>Usage: /report <player> <reason>");
            player.sendMessage(msg);
            return true;
        }

        String targetName = args[0];
        OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(targetName);

        if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
            Component msg = langManager.getMessageFor(player, "reports.command.invalid-player",
                    "<red>Player <yellow>{player}</yellow> not found.",
                    LanguageManager.ComponentPlaceholder.of("{player}", targetName));
            player.sendMessage(msg);
            return true;
        }

        storage.getCooldown(player.getUniqueId()).thenAccept(lastUsed -> {
            long now = System.currentTimeMillis();
            long cdMillis = config.cooldown * 1000L;

            if (now - lastUsed < cdMillis) {
                long remainingSeconds = (cdMillis - (now - lastUsed)) / 1000;
                Component msg = langManager.getMessageFor(player, "reports.cooldown",
                        "<red>Please wait <yellow>{time}</yellow> seconds before reporting again.",
                        LanguageManager.ComponentPlaceholder.of("{time}", String.valueOf(remainingSeconds)));
                player.sendMessage(msg);
                return;
            }

            StringBuilder reasonBuilder = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                if (i > 1) reasonBuilder.append(" ");
                reasonBuilder.append(args[i]);
            }
            String reason = reasonBuilder.toString();

            if (reason.length() > config.maxReasonLength) {
                Component msg = langManager.getMessageFor(player, "reports.reason-too-long",
                        "<red>Reason too long! Max <yellow>{max}</yellow> characters.",
                        LanguageManager.ComponentPlaceholder.of("{max}", String.valueOf(config.maxReasonLength)));
                player.sendMessage(msg);
                return;
            }

            String reportId = UUID.randomUUID().toString().substring(0, 8);
            Report report = new Report(reportId, player.getUniqueId(), target.getUniqueId(), reason, System.currentTimeMillis(), true);

            storage.addReport(report).thenRun(() -> {
                storage.saveCooldown(player.getUniqueId(), System.currentTimeMillis());

                Component msg = langManager.getMessageFor(player, "reports.command.submitted",
                        "<green>Report submitted against <yellow>{player}</yellow>.",
                        LanguageManager.ComponentPlaceholder.of("{player}", target.getName()));
                player.sendMessage(msg);

                notifyStaff(report, target.getName());
            }).exceptionally(ex -> {
                player.sendMessage(langManager.getMessageFor(player, "reports.error",
                        "<red>Error submitting report. Please try again."));
                plugin.getLogger().warning("Failed to submit report: " + ex.getMessage());
                return null;
            });
        });

        return true;
    }

    private void notifyStaff(Report report, String targetName) {
        Component notifyMsg = langManager.getMessageFor(null, "reports.command.notify",
                "<red>[REPORT]</red> <yellow>{reporter}</yellow> reported <gold>{target}</gold> for <gray>{reason}</gray> <dark_gray>(ID: {id})</dark_gray>",
                LanguageManager.ComponentPlaceholder.of("{id}", report.id()),
                LanguageManager.ComponentPlaceholder.of("{reporter}", Bukkit.getOfflinePlayer(report.reporterId()).getName()),
                LanguageManager.ComponentPlaceholder.of("{target}", targetName != null ? targetName : "Unknown"),
                LanguageManager.ComponentPlaceholder.of("{reason}", report.reason()));

        boolean notified = false;
        for (Player admin : Bukkit.getOnlinePlayers()) {
            if (admin.hasPermission("serveressentials.command.report.*")) {
                admin.sendMessage(notifyMsg);
                notified = true;
            }
        }

        if (!notified && !config.notifyOnlineOnly) {
            storage.markAsCleared(report.id()); // Mark as pending
        }
    }

    private boolean handleClear(CommandSender sender, String[] args) {
        if (!sender.hasPermission("serveressentials.command.report.*")) {
            Player player = getPlayerOrNull(sender);
            Component msg = langManager.getMessageFor(player,
                    "commands.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    LanguageManager.ComponentPlaceholder.of("{permission}", "serveressentials.command.report.*"));

            if (player != null) {
                player.sendMessage(msg);
            } else {
                sender.sendMessage(msg.toString());
            }
            return true;
        }

        if (args.length < 1) {
            Player player = getPlayerOrNull(sender);
            Component msg = langManager.getMessageFor(player,
                    "reports.commandclear.usage",
                    "<red>Usage: /reportclear <id>");

            if (player != null) {
                player.sendMessage(msg);
            } else {
                sender.sendMessage(msg.toString());
            }
            return true;
        }

        String id = args[0];
        storage.getReport(id).thenAccept(optReport -> {
            if (optReport.isEmpty()) {
                Player player = getPlayerOrNull(sender);
                Component msg = langManager.getMessageFor(player,
                        "reports.commandclear.invalid",
                        "<red>Invalid report ID: <yellow>{id}</yellow>",
                        LanguageManager.ComponentPlaceholder.of("{id}", id));

                if (player != null) {
                    player.sendMessage(msg);
                } else {
                    sender.sendMessage(msg.toString());
                }
                return;
            }

            storage.clearReport(id).thenRun(() -> {
                Player player = getPlayerOrNull(sender);
                Component msg = langManager.getMessageFor(player,
                        "reports.commandclear.cleared",
                        "<green>Cleared report ID: <yellow>{id}</yellow>",
                        LanguageManager.ComponentPlaceholder.of("{id}", id));

                if (player != null) {
                    player.sendMessage(msg);
                } else {
                    sender.sendMessage(msg.toString());
                }
            }).exceptionally(ex -> {
                Player player = getPlayerOrNull(sender);
                Component msg = player != null ?
                        langManager.getMessageFor(player, "reports.error", "<red>Error clearing report. Please try again.") :
                        langManager.getMessageFor(null, "reports.error", "<red>Error clearing report. Please try again.");

                if (player != null) {
                    player.sendMessage(msg);
                } else {
                    sender.sendMessage(msg.toString());
                }
                plugin.getLogger().warning("Failed to clear report: " + ex.getMessage());
                return null;
            });
        });

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String cmd = command.getName().toLowerCase();

        if (cmd.equals("report") && args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }

        if (cmd.equals("reportclear") && args.length == 1) {
            return List.of();
        }

        return List.of();
    }

    public void handleJoin(Player player) {
        if (!player.hasPermission("serveressentials.command.report.*")) return;

        storage.getPendingReports().thenAccept(reports -> {
            if (reports.isEmpty()) return;

            Component header = langManager.getMessageFor(player, "reports.reportslist.header",
                    "<gold>You have pending reports to review:");
            player.sendMessage(header);

            for (Report report : reports) {
                String reporterName = Bukkit.getOfflinePlayer(report.reporterId()).getName();
                String targetName = Bukkit.getOfflinePlayer(report.targetId()).getName();

                Component entry = langManager.getMessageFor(player, "reports.reportslist.entry",
                        "<red>[PENDING]</red> <yellow>{reporter}</yellow> → <gold>{target}</gold>: <gray>{reason}</gray> <dark_gray>(ID: {id})</dark_gray>",
                        LanguageManager.ComponentPlaceholder.of("{id}", report.id()),
                        LanguageManager.ComponentPlaceholder.of("{reporter}", reporterName != null ? reporterName : "Unknown"),
                        LanguageManager.ComponentPlaceholder.of("{target}", targetName != null ? targetName : "Unknown"),
                        LanguageManager.ComponentPlaceholder.of("{reason}", report.reason()));
                player.sendMessage(entry);
            }

            reports.forEach(report -> storage.markAsCleared(report.id()));
        });
    }
}