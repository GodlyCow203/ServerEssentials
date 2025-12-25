package net.godlycow.org.commands.impl;

import net.godlycow.org.reports.model.Report;
import net.godlycow.org.reports.storage.ReportStorage;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.godlycow.org.language.LanguageManager;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ReportsListCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final PlayerLanguageManager langManager;
    private final ReportStorage storage;
    private final BukkitAudiences audiences;

    public ReportsListCommand(JavaPlugin plugin, PlayerLanguageManager langManager, ReportStorage storage) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.storage = storage;
        this.audiences = BukkitAudiences.create(plugin);
    }

    private Audience audience(CommandSender sender) {
        return audiences.sender(sender);
    }

    private Player getPlayerOrNull(CommandSender sender) {
        return sender instanceof Player ? (Player) sender : null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Audience audience = audience(sender);
        Player player = getPlayerOrNull(sender);

        if (!sender.hasPermission("serveressentials.commandreport.*")) {
            Component msg = langManager.getMessageFor(
                    player,
                    "commands.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    LanguageManager.ComponentPlaceholder.of("{permission}", "serveressentials.command.report.*")
            );
            audience.sendMessage(msg);
            return true;
        }

        storage.getAllReports().thenAccept(reports -> {

            if (reports.isEmpty()) {
                Component msg = langManager.getMessageFor(
                        player,
                        "reports.list.none",
                        "<yellow>No reports found."
                );
                audience.sendMessage(msg);
                return;
            }

            Component header = langManager.getMessageFor(
                    player,
                    "reports.list.header",
                    "<gold>----- <yellow>All Reports</yellow> <gold>-----"
            );
            audience.sendMessage(header);

            for (Report report : reports) {

                String reporterName = Bukkit.getOfflinePlayer(report.reporterId()).getName();
                String targetName = Bukkit.getOfflinePlayer(report.targetId()).getName();

                Component entry = langManager.getMessageFor(
                        player,
                        "reports.list.entry",
                        "<gray>[{id}] <yellow>{reporter}</yellow> → <gold>{target}</gold>: <white>{reason}</white>",
                        LanguageManager.ComponentPlaceholder.of("{id}", report.id()),
                        LanguageManager.ComponentPlaceholder.of("{reporter}", reporterName != null ? reporterName : "Unknown"),
                        LanguageManager.ComponentPlaceholder.of("{target}", targetName != null ? targetName : "Unknown"),
                        LanguageManager.ComponentPlaceholder.of("{reason}", report.reason())
                );

                audience.sendMessage(entry);
            }

            Component footer = langManager.getMessageFor(
                    player,
                    "reports.list.footer",
                    "<gold>Total: <yellow>{count}</yellow> reports",
                    LanguageManager.ComponentPlaceholder.of("{count}", String.valueOf(reports.size()))
            );

            audience.sendMessage(footer);
        });

        return true;
    }
}
