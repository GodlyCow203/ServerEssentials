package net.godlycow.org.commands.impl;

import net.godlycow.org.ban.storage.BanStorage;
import net.godlycow.org.commands.CommandModule;
import net.godlycow.org.language.LanguageManager;
import net.godlycow.org.language.PlayerLanguageManager;
import net.godlycow.org.commands.CommandDataStorage;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.*;

public class BanListCommand extends CommandModule implements CommandExecutor {
    private final PlayerLanguageManager langManager;
    private final BanStorage storage;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public BanListCommand(PlayerLanguageManager langManager,
                          CommandDataStorage commandStorage, BanStorage storage) {
        super(commandStorage, langManager);
        this.langManager = langManager;
        this.storage = storage;
    }

    @Override
    protected String getCommandName() {
        return "banlist";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("essc.command.banlist")) {
            sender.sendMessage(langManager.getMessageFor(sender instanceof Player ? (Player)sender : null,
                    "commands.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    LanguageManager.ComponentPlaceholder.of("{permission}", "essc.command.banlist")));
            return true;
        }

        storage.getAllBannedUUIDs().thenAccept(uuids -> {
            if (uuids.isEmpty()) {
                sender.sendMessage(langManager.getMessageFor(sender instanceof Player ? (Player)sender : null,
                        "commands.banlist.empty",
                        "<yellow>There are no banned players."));
                return;
            }

            sender.sendMessage(langManager.getMessageFor(sender instanceof Player ? (Player)sender : null,
                    "commands.banlist.header",
                    "<gold>------[ Banned Players ]------"));

            uuids.forEach(uuid -> {
                storage.getBanData(UUID.fromString(uuid)).thenAccept(optData -> {
                    optData.ifPresent(data -> {
                        String time = data.until() == -1 ? "Permanent" : dateFormat.format(new Date(data.until()));
                        Component line = langManager.getMessageFor(sender instanceof Player ? (Player)sender : null,
                                "commands.banlist.format",
                                "<red>{player} <gray>- <white>{reason} <gray>(<aqua>{time}</aqua>)",
                                LanguageManager.ComponentPlaceholder.of("{player}", data.name()),
                                LanguageManager.ComponentPlaceholder.of("{reason}", data.reason()),
                                LanguageManager.ComponentPlaceholder.of("{time}", time));
                        sender.sendMessage(line);
                    });
                });
            });

            sender.sendMessage(langManager.getMessageFor(sender instanceof Player ? (Player)sender : null,
                    "commands.banlist.footer",
                    "<gold>-----------------------------"));
        });

        return true;
    }
}