package net.godlycow.org.commands;

import net.kyori.adventure.text.Component;
import net.godlycow.org.database.DatabaseManager;
import net.godlycow.org.language.LanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Map;

public class DatabaseCommand implements CommandExecutor {
    private final DatabaseManager databaseManager;
    private final LanguageManager languageManager;

    public DatabaseCommand(DatabaseManager databaseManager, LanguageManager languageManager) {
        this.databaseManager = databaseManager;
        this.languageManager = languageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Map<String, DatabaseManager.DatabaseStatus> statusMap = databaseManager.getAllStatus();

        Component header = languageManager.getComponent("database.header",
                "<dark_aqua>╔════════════════════════════════════╗");
        Component title = languageManager.getComponent("database.title",
                "<dark_aqua>║  <bold>Database Status</bold>                     ║");
        Component separator = languageManager.getComponent("database.separator",
                "<dark_aqua>╠════════════════════════════════════╣");

        sender.sendMessage(header);
        sender.sendMessage(title);
        sender.sendMessage(separator);

        if (statusMap.isEmpty()) {
            Component none = languageManager.getComponent("database.none",
                    "<red>No databases configured!");
            sender.sendMessage(none);
        } else {
            statusMap.forEach((key, status) -> {
                String statusColor = status.connected() ? "<#32E800>" : "<#FF0000>";
                String enabledText = status.connected() ? "CONNECTED" : "ERROR";
                String message = status.connected() ? "OK" : status.message();

                Component entry = languageManager.getComponent("database.entry",
                        "<dark_aqua>║  <yellow>{name}</yellow> - {status}{msg} <gray>({enabled})</gray>  ║",
                        LanguageManager.ComponentPlaceholder.of("{name}", key),
                        LanguageManager.ComponentPlaceholder.of("{status}", statusColor),
                        LanguageManager.ComponentPlaceholder.of("{msg}", message),
                        LanguageManager.ComponentPlaceholder.of("{enabled}", enabledText));

                sender.sendMessage(entry);
            });

            if (args.length > 0 && args[0].equalsIgnoreCase("kits")) {
                showKitStats(sender);
            }
        }

        Component footer = languageManager.getComponent("database.footer",
                "<dark_aqua>╚════════════════════════════════════╝");
        sender.sendMessage(footer);

        return true;
    }

    private void showKitStats(CommandSender sender) {
        Component kitTitle = languageManager.getComponent("database.kit-stats",
                "<gold>Kit Database Stats:</gold> <gray>Table: kit_claims</gray>");
        sender.sendMessage(kitTitle);
    }
}