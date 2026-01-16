package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.CommandModule;
import net.godlycow.org.commands.config.MuteConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.language.LanguageManager;
import net.godlycow.org.mute.storage.MuteStorage;
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

        Bukkit.getScheduler().runTaskTimer(plugin, this::checkExpiredMutes,
                20L, config.checkInterval.toSeconds() * 20L);
    }

    @Override
    protected String getCommandName() {
        return "mute";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("essc.command.mute")) {
            sender.sendMessage(langManager.getMessageFor(sender instanceof Player ? (Player)sender : null,
                    "commands.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    LanguageManager.ComponentPlaceholder.of("{permission}", "essc.command.mute")));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(langManager.getMessageFor(sender instanceof Player ? (Player)sender : null,
                    "commands.mute.usage",
                    "<red>Usage: <yellow>/mute <player> <duration> <reason> | /mute <player> <reason> [perm]"));
            return true;
        }

        String targetName = args[0];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (target.getName() == null) {
            sender.sendMessage(langManager.getMessageFor(sender instanceof Player ? (Player)sender : null,
                    "commands.mute.not-found",
                    "<red>Player not found."));
            return true;
        }

        String durationStr;
        String reason;

        if (args.length >= 3 && args[1].matches("\\d+[smhdSMHD].*")) {
            durationStr = args[1];
            reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        } else {
            durationStr = "perm";
            reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        }

        long expiresAt = parseDuration(durationStr);
        storage.isMuted(target.getUniqueId()).thenCompose(isMuted -> {
            if (isMuted) {
                return storage.getMuteData(target.getUniqueId()).thenApply(opt -> opt.map(MuteStorage.MuteData::reason).orElse("No reason"));
            }
            return CompletableFuture.completedFuture(null);
        }).thenAccept(existingReason -> {
            if (existingReason != null) {
                sender.sendMessage(langManager.getMessageFor(sender instanceof Player ? (Player)sender : null,
                        "commands.mute.already-muted",
                        "<red><yellow>{player}</yellow> is already muted for <white>{reason}</white>.",
                        LanguageManager.ComponentPlaceholder.of("{player}", target.getName()),
                        LanguageManager.ComponentPlaceholder.of("{reason}", existingReason)));
                return;
            }

            storage.mutePlayer(target.getUniqueId(), reason, expiresAt).thenRun(() -> {
                String durationDisplay = formatDurationDisplay(expiresAt);

                sender.sendMessage(langManager.getMessageFor(sender instanceof Player ? (Player)sender : null,
                        "commands.mute.success",
                        "<yellow>You muted <green>{player}</green> <gray>for <white>{reason}</white> <yellow>({duration})</yellow>.",
                        LanguageManager.ComponentPlaceholder.of("{player}", target.getName()),
                        LanguageManager.ComponentPlaceholder.of("{reason}", reason),
                        LanguageManager.ComponentPlaceholder.of("{duration}", durationDisplay)));

                if (target.isOnline()) {
                    Player player = (Player) target.getPlayer();
                    player.sendMessage(langManager.getMessageFor(player,
                            "commands.mute.notify",
                            "<red>You have been muted! <gray>Reason: <white>{reason}</white> <yellow>({duration})</yellow>",
                            LanguageManager.ComponentPlaceholder.of("{reason}", reason),
                            LanguageManager.ComponentPlaceholder.of("{duration}", durationDisplay)));
                }
            });
        });

        return true;
    }

    private long parseDuration(String input) {
        if (input.equalsIgnoreCase("perm") || input.equalsIgnoreCase("permanent")) {
            return -1;
        }

        long totalMs = 0;
        StringBuilder currentNumber = new StringBuilder();

        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                currentNumber.append(c);
            } else if (Character.isLetter(c)) {
                if (currentNumber.length() == 0) {
                    return System.currentTimeMillis();
                }

                long value = Long.parseLong(currentNumber.toString());
                currentNumber.setLength(0);

                long multiplier = switch (Character.toLowerCase(c)) {
                    case 's' -> 1000L;
                    case 'm' -> 60 * 1000L;
                    case 'h' -> 60 * 60 * 1000L;
                    case 'd' -> 24 * 60 * 60 * 1000L;
                    default -> 0;
                };

                if (multiplier == 0) {
                    return System.currentTimeMillis();
                }

                totalMs += value * multiplier;
            }
        }

        if (currentNumber.length() > 0) {
            totalMs += Long.parseLong(currentNumber.toString()) * 1000L;
        }

        if (totalMs == 0) {
            return System.currentTimeMillis();
        }

        return System.currentTimeMillis() + totalMs;
    }
    private String formatDurationDisplay(long expiresAt) {
        if (expiresAt == -1) {
            return "Permanent";
        }

        long remainingMs = expiresAt - System.currentTimeMillis();
        if (remainingMs <= 0) {
            return "Expired";
        }

        long seconds = remainingMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        StringBuilder display = new StringBuilder();
        if (days > 0) {
            display.append(days).append("d ");
        }
        if (hours % 24 > 0) {
            display.append(hours % 24).append("h ");
        }
        if (minutes % 60 > 0 && days == 0) {
            display.append(minutes % 60).append("m ");
        }
        if (seconds % 60 > 0 && hours == 0 && days == 0) {
            display.append(seconds % 60).append("s");
        }

        return display.toString().trim();
    }

    private void checkExpiredMutes() {
        storage.getAllMutedUUIDs().thenAccept(uuids -> {
            uuids.forEach(uuid -> {
                storage.isMuted(uuid).thenAccept(isMuted -> {
                    if (!isMuted) {
                        storage.unmutePlayer(uuid).thenRun(() -> {
                            Player player = Bukkit.getPlayer(uuid);
                            if (player != null && player.isOnline()) {
                                player.sendMessage(langManager.getMessageFor(player,
                                        "commands.unmute.notify",
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