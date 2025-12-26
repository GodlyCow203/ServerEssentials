package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.config.NickConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import net.godlycow.org.nick.NickManager;
import net.godlycow.org.nick.storage.NickStorage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.regex.Pattern;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class NickCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.nick";
    private static final String ADMIN_PERMISSION = "serveressentials.command.nicks";

    private final NickConfig config;
    private final PlayerLanguageManager langManager;
    private final NickStorage nickStorage;
    private final NickManager nickManager;

    public NickCommand(PlayerLanguageManager langManager, NickConfig config,
                       NickStorage nickStorage, NickManager nickManager) {
        this.langManager = langManager;
        this.config = config;
        this.nickStorage = nickStorage;
        this.nickManager = nickManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!config.enabled) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.nick.disabled",
                    "<red>The nickname system is currently disabled.").toString());
            return true;
        }

        if (command.getName().equalsIgnoreCase("nick")) {
            return handleNick(sender, args);
        } else if (command.getName().equalsIgnoreCase("nicks")) {
            return handleNicks(sender, args);
        }

        return false;
    }

    private boolean handleNick(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.nick.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.nick.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(langManager.getMessageFor(player, "commands.nick.usage",
                    "<yellow>Usage: <white>/nick <nickname> or /nick reset").toString());
            return true;
        }

        if (config.allowReset && args[0].equalsIgnoreCase("reset")) {
            return resetOwnNick(player);
        }

        return setNick(player, String.join(" ", args));
    }

    private boolean setNick(Player player, String newNick) {
        if (!isValidNick(player, newNick)) return true;

        UUID uuid = player.getUniqueId();

        nickStorage.getNickname(uuid).thenAccept(opt -> {
            opt.ifPresent(existingNick -> {
                if (config.cooldown > 0) {
                    long lastChange = System.currentTimeMillis() - (config.cooldown * 1000);
                }
            });

            if (config.maxChangesPerDay > 0) {
                String today = java.time.LocalDate.now().toString();
                nickStorage.getDailyChanges(uuid, today).thenAccept(changesOpt -> {
                    int changes = changesOpt.orElse(0);
                    if (changes >= config.maxChangesPerDay) {
                        player.sendMessage(langManager.getMessageFor(player, "commands.nick.daily-limit",
                                "<red>You can only change your nickname <yellow>{limit}</yellow> times per day.",
                                ComponentPlaceholder.of("{limit}", config.maxChangesPerDay)));
                        return;
                    }
                    proceedWithNickChange(player, newNick, today, changes);
                });
            } else {
                proceedWithNickChange(player, newNick, null, 0);
            }
        });

        return true;
    }

    private void proceedWithNickChange(Player player, String newNick, String day, int dailyChanges) {
        UUID uuid = player.getUniqueId();

        nickStorage.setNickname(uuid, newNick).thenAccept(v -> {
            nickManager.applyNick(uuid, newNick);

            player.sendMessage(langManager.getMessageFor(player, "commands.nick.set",
                    "<green>Your nickname has been set to <white>{nick}</white>.",
                    ComponentPlaceholder.of("{nick}", newNick)));

            if (day != null) {
                nickStorage.incrementDailyChanges(uuid);
            }
        }).exceptionally(ex -> {
            player.sendMessage(langManager.getMessageFor(player, "commands.nick.error",
                    "<red>Failed to save nickname. Please try again."));
            return null;
        });
    }

    private boolean resetOwnNick(Player player) {
        UUID uuid = player.getUniqueId();

        nickStorage.removeNickname(uuid).thenAccept(v -> {
            nickManager.removeNick(uuid);
            player.sendMessage(langManager.getMessageFor(player, "commands.nick.reset-self",
                    "<green>Your nickname has been reset."));
        }).exceptionally(ex -> {
            player.sendMessage(langManager.getMessageFor(player, "commands.nick.error",
                    "<red>Failed to reset nickname. Please try again."));
            return null;
        });

        return true;
    }

    private boolean handleNicks(CommandSender sender, String[] args) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(langManager.getMessageFor(sender instanceof Player p ? p : null,
                    "commands.nicks.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", ADMIN_PERMISSION)).toString());
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.nicks.usage",
                    "<yellow>Usage: <white>/nicks reload|reset <player>").toString());
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            return reloadNicks(sender);
        }

        if (args[0].equalsIgnoreCase("reset") && args.length == 2) {
            return resetOtherNick(sender, args[1]);
        }

        return false;
    }

    private boolean reloadNicks(CommandSender sender) {
        config.reload();
        nickManager.reloadAllNicks();

        sender.sendMessage(langManager.getMessageFor(sender instanceof Player p ? p : null,
                "commands.nicks.reload",
                "<green>Nickname system reloaded successfully."));
        return true;
    }

    private boolean resetOtherNick(CommandSender sender, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(langManager.getMessageFor(sender instanceof Player p ? p : null,
                    "commands.nicks.not-found",
                    "<red>Player <yellow>{player}</yellow> not found.",
                    ComponentPlaceholder.of("{player}", targetName)).toString());
            return true;
        }

        UUID uuid = target.getUniqueId();
        nickStorage.removeNickname(uuid).thenAccept(v -> {
            nickManager.removeNick(uuid);
            sender.sendMessage(langManager.getMessageFor(sender instanceof Player p ? p : null,
                    "commands.nicks.reset",
                    "<green>Reset nickname for <yellow>{player}</yellow>.",
                    ComponentPlaceholder.of("{player}", target.getName())));
        }).exceptionally(ex -> {
            sender.sendMessage(langManager.getMessageFor(sender instanceof Player p ? p : null,
                    "commands.nicks.error",
                    "<red>Failed to reset nickname. Please try again."));
            return null;
        });

        return true;
    }

    private boolean isValidNick(Player player, String nick) {
        String stripped = stripFormatting(nick);

        if (stripped.length() < config.minLength || stripped.length() > config.maxLength) {
            player.sendMessage(langManager.getMessageFor(player, "commands.nick.invalid-length",
                    "<red>Nickname must be between <yellow>{min}</yellow> and <yellow>{max}</yellow> characters.",
                    ComponentPlaceholder.of("{min}", config.minLength),
                    ComponentPlaceholder.of("{max}", config.maxLength)));
            return false;
        }

        for (String word : config.blockedWords) {
            if (stripped.toLowerCase().contains(word.toLowerCase())) {
                player.sendMessage(langManager.getMessageFor(player, "commands.nick.blocked-word",
                        "<red>The word <yellow>{word}</yellow> is not allowed.",
                        ComponentPlaceholder.of("{word}", word)));
                return false;
            }
        }

        for (String pattern : config.blacklistPatterns) {
            try {
                if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(stripped).find()) {
                    player.sendMessage(langManager.getMessageFor(player, "commands.nick.blacklisted",
                            "<red>This nickname format is not allowed."));
                    return false;
                }
            } catch (Exception ignored) {}
        }

        if (!config.allowFormatting && containsFormatting(nick)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.nick.no-formatting",
                    "<red>Formatting is not allowed in nicknames."));
            return false;
        }

        if (!config.allowDuplicates) {
            for (java.util.Map.Entry<UUID, String> entry : nickManager.getAllCachedNicks().entrySet()) {
                if (entry.getValue().equalsIgnoreCase(nick) && !entry.getKey().equals(player.getUniqueId())) {
                    player.sendMessage(langManager.getMessageFor(player, "commands.nick.duplicate",
                            "<red>This nickname is already taken by another player."));
                    return false;
                }
            }
        }

        return true;
    }

    private String stripFormatting(String input) {
        return input.replaceAll("<[^>]+>", "");
    }

    private boolean containsFormatting(String input) {
        return input.contains("<") && input.contains(">");
    }
}