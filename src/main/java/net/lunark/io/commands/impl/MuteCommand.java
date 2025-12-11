package net.lunark.io.commands.impl;

import net.lunark.io.commands.CommandModule;
import net.lunark.io.commands.config.MuteConfig;
import net.lunark.io.language.PlayerLanguageManager;
import net.lunark.io.commands.CommandDataStorage;
import net.lunark.io.language.LanguageManager;
import net.kyori.adventure.text.Component;
import net.lunark.io.mute.MuteStorage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class MuteCommand extends CommandModule implements CommandExecutor {
    private final JavaPlugin plugin;
    private final PlayerLanguageManager langManager;
    private final MuteStorage storage;
    private final MuteConfig config;

    public MuteCommand(JavaPlugin plugin, PlayerLanguageManager langManager,
                       CommandDataStorage commandStorage, MuteStorage storage, MuteConfig config) {
        super(commandStorage, langManager);
        this.plugin = plugin;
        this.langManager = langManager;
        this.storage = storage;
        this.config = config;

        // Start expiration checker
        Bukkit.getScheduler().runTaskTimer(plugin, this::checkExpiredMutes,
                20L, config.checkInterval.toSeconds() * 20L);
    }

    @Override
    protected String getCommandName() {
        return "mute";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("serveressentials.command.mute")) {
            sender.sendMessage(langManager.getMessageFor(sender instanceof Player ? (Player)sender : null,
                    "commands.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    LanguageManager.ComponentPlaceholder.of("{permission}", "serveressentials.command.mute")));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(langManager.getMessageFor(sender instanceof Player ? (Player)sender : null,
                    "mute.usage",
                    "<red>Usage: <yellow>/mute <player> <reason> [duration]"));
            return true;
        }

        String targetName = args[0];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (target.getName() == null) {
            sender.sendMessage(langManager.getMessageFor(sender instanceof Player ? (Player)sender : null,
                    "mute.not-found",
                    "<red>Player not found."));
            return true;
        }

        String reason = args[1];
        String durationStr = args.length > 2 ? args[2] : "perm";
        long expiresAt = parseDuration(durationStr);

        // Check if already muted
        storage.isMuted(target.getUniqueId()).thenCompose(isMuted -> {
            if (isMuted) {
                return storage.getMuteData(target.getUniqueId()).thenApply(opt -> opt.map(MuteStorage.MuteData::reason).orElse("No reason"));
            }
            return CompletableFuture.completedFuture(null);
        }).thenAccept(existingReason -> {
            if (existingReason != null) {
                sender.sendMessage(langManager.getMessageFor(sender instanceof Player ? (Player)sender : null,
                        "mute.already-muted",
                        "<red><yellow>{player}</yellow> is already muted for <white>{reason}</white>.",
                        LanguageManager.ComponentPlaceholder.of("{player}", target.getName()),
                        LanguageManager.ComponentPlaceholder.of("{reason}", existingReason)));
                return;
            }

            // Apply mute
            storage.mutePlayer(target.getUniqueId(), reason, expiresAt).thenRun(() -> {
                // Notify staff
                sender.sendMessage(langManager.getMessageFor(sender instanceof Player ? (Player)sender : null,
                        "mute.success",
                        "<yellow>You muted <green>{player}</green> <gray>for <white>{reason}</white> <yellow>({duration})</yellow>.",
                        LanguageManager.ComponentPlaceholder.of("{player}", target.getName()),
                        LanguageManager.ComponentPlaceholder.of("{reason}", reason),
                        LanguageManager.ComponentPlaceholder.of("{duration}", durationStr)));

                // Notify player if online
                if (target.isOnline()) {
                    Player player = (Player) target.getPlayer();
                    player.sendMessage(langManager.getMessageFor(player,
                            "mute.notify",
                            "<red>You have been muted! <gray>Reason: <white>{reason}</white> <yellow>({duration})</yellow>",
                            LanguageManager.ComponentPlaceholder.of("{reason}", reason),
                            LanguageManager.ComponentPlaceholder.of("{duration}", durationStr)));
                }
            });
        });

        return true;
    }

    private long parseDuration(String input) {
        if (input.equalsIgnoreCase("perm")) return -1;
        try {
            long time = Long.parseLong(input.substring(0, input.length() - 1));
            char unit = input.charAt(input.length() - 1);
            return switch (unit) {
                case 's' -> System.currentTimeMillis() + time * 1000L;
                case 'm' -> System.currentTimeMillis() + time * 60 * 1000L;
                case 'h' -> System.currentTimeMillis() + time * 60 * 60 * 1000L;
                case 'd' -> System.currentTimeMillis() + time * 24 * 60 * 60 * 1000L;
                default -> -1;
            };
        } catch (Exception e) {
            return -1;
        }
    }

    private void checkExpiredMutes() {
        storage.getAllMutedUUIDs().thenAccept(uuids -> {
            uuids.forEach(uuid -> {
                storage.isMuted(uuid).thenAccept(isMuted -> {
                    if (!isMuted) {
                        // Expired, remove
                        storage.unmutePlayer(uuid).thenRun(() -> {
                            Player player = Bukkit.getPlayer(uuid);
                            if (player != null && player.isOnline()) {
                                player.sendMessage(langManager.getMessageFor(player,
                                        "unmute.notify",
                                        "<green>You have been unmuted!"));
                            }
                        });
                    }
                });
            });
        });
    }

    public CompletableFuture<Void> unmute(UUID uuid) {
        return storage.unmutePlayer(uuid);
    }

    public CompletableFuture<Boolean> isMuted(UUID uuid) {
        return storage.isMuted(uuid);
    }

    public CompletableFuture<Optional<String>> getMuteReason(UUID uuid) {
        return storage.getMuteData(uuid).thenApply(opt -> opt.map(MuteStorage.MuteData::reason));
    }
}