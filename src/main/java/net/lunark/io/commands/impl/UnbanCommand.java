package net.lunark.io.commands.impl;

import net.lunark.io.ban.BanStorage;
import net.lunark.io.commands.CommandModule;
import net.lunark.io.language.PlayerLanguageManager;
import net.lunark.io.commands.CommandDataStorage;
import net.lunark.io.language.LanguageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class UnbanCommand extends CommandModule implements CommandExecutor {
    private final PlayerLanguageManager langManager;
    private final BanStorage storage;

    public UnbanCommand(PlayerLanguageManager langManager,
                        CommandDataStorage commandStorage, BanStorage storage) {
        super(commandStorage, langManager);
        this.langManager = langManager;
        this.storage = storage;
    }

    @Override
    protected String getCommandName() {
        return "unban";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("serveressentials.command.unban")) {
            sender.sendMessage(langManager.getMessageFor(sender instanceof Player ? (Player)sender : null,
                    "commands.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    LanguageManager.ComponentPlaceholder.of("{permission}", "serveressentials.command.unban")));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(langManager.getMessageFor(sender instanceof Player ? (Player)sender : null,
                    "unban.usage",
                    "<red>Usage: <yellow>/unban <player>"));
            return true;
        }

        String playerName = args[0];

        storage.getUUIDFromName(playerName).thenCompose(optUuid -> {
            if (optUuid.isEmpty()) {
                sender.sendMessage(langManager.getMessageFor(sender instanceof Player ? (Player)sender : null,
                        "unban.player-not-found",
                        "<red>Could not find banned player with name '<yellow>{player}</yellow>'.",
                        LanguageManager.ComponentPlaceholder.of("{player}", playerName)));
                return CompletableFuture.completedFuture(null);
            }

            UUID uuid = optUuid.get();

            return storage.isBanned(uuid).thenCompose(isBanned -> {
                if (!isBanned) {
                    sender.sendMessage(langManager.getMessageFor(sender instanceof Player ? (Player)sender : null,
                            "unban.not-banned",
                            "<yellow>{player}</yellow> is not currently banned.",
                            LanguageManager.ComponentPlaceholder.of("{player}", playerName)));
                    return CompletableFuture.completedFuture(null);
                }

                return storage.unbanPlayer(uuid).thenApply(v -> playerName);
            });
        }).thenAccept(result -> {
            if (result != null) {
                sender.sendMessage(langManager.getMessageFor(sender instanceof Player ? (Player)sender : null,
                        "unban.success",
                        "<green>{player}</green> has been unbanned.",
                        LanguageManager.ComponentPlaceholder.of("{player}", result)));
            }
        });

        return true;
    }
}