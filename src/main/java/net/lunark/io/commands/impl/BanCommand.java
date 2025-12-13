package net.lunark.io.commands.impl;


import net.lunark.io.ban.BanStorage;
import net.lunark.io.commands.CommandModule;
import net.lunark.io.commands.config.BanConfig;
import net.lunark.io.language.LanguageManager;
import net.lunark.io.language.PlayerLanguageManager;
import net.lunark.io.commands.CommandDataStorage;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class BanCommand extends CommandModule implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;
    private final PlayerLanguageManager langManager;
    private final BanStorage storage;
    private final BanConfig config;


    public BanCommand(JavaPlugin plugin, PlayerLanguageManager langManager,
                      CommandDataStorage commandStorage, BanStorage storage, BanConfig config) {
        super(commandStorage, langManager);
        this.plugin = plugin;
        this.langManager = langManager;
        this.storage = storage;
        this.config = config;
    }

    @Override
    protected String getCommandName() {
        return "ban";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("serveressentials.command.ban")) {
            sender.sendMessage(langManager.getMessageFor(sender instanceof Player ? (Player) sender : null,
                    "commands.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    LanguageManager.ComponentPlaceholder.of("{permission}", "serveressentials.command.ban")));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(langManager.getMessageFor(sender instanceof Player ? (Player) sender : null,
                    "ban.usage",
                    "<red>Usage: <yellow>/ban <player> <time> <reason>"));
            return true;
        }

        String targetName = args[0];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (target.getName() == null) {
            sender.sendMessage(langManager.getMessageFor(sender instanceof Player ? (Player) sender : null,
                    "ban.never-joined",
                    "<red>That player has never joined the server."));
            return true;
        }

        long duration = parseTime(args[1]);
        if (duration == -2) {
            sender.sendMessage(langManager.getMessageFor(sender instanceof Player ? (Player) sender : null,
                    "ban.invalid-time",
                    "<red>Invalid time format. Use <yellow>s/m/h/d</yellow> or <yellow>perm</yellow>"));
            return true;
        }

        String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        String bannedBy = (sender instanceof Player) ? ((Player) sender).getName() : "Console";
        long until = (duration == -1) ? -1 : System.currentTimeMillis() + duration;

        storage.banPlayer(target.getUniqueId(), target.getName(), reason, bannedBy, until,
                config.serverName, config.discordLink).thenRun(() -> {

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (target.isOnline()) {
                    Component kickMsg = langManager.getMessageFor(target.getPlayer(),
                            "ban.kick-message",
                            "<red>You have been banned!\n<gray>Reason: <yellow>{reason}</yellow>\n<gray>Banned by: <yellow>{banner}</yellow>\n<gray>Until: <yellow>{until}</yellow>\n<gray>Appeal at: <aqua>{discord}</aqua>",
                            LanguageManager.ComponentPlaceholder.of("{reason}", reason),
                            LanguageManager.ComponentPlaceholder.of("{banner}", bannedBy),
                            LanguageManager.ComponentPlaceholder.of("{until}", until == -1 ? "Permanent" : new Date(until).toString()),
                            LanguageManager.ComponentPlaceholder.of("{discord}", config.discordLink));
                    target.getPlayer().kick(kickMsg);
                }

                sender.sendMessage(langManager.getMessageFor(sender instanceof Player ? (Player) sender : null,
                        "ban.success",
                        "<green><yellow>{player}</yellow> has been banned.",
                        LanguageManager.ComponentPlaceholder.of("{player}", target.getName())));
            });
        });

        return true;
    }

    private long parseTime(String time) {
        if (time.equalsIgnoreCase("perm")) return -1;
        try {
            long num = Long.parseLong(time.substring(0, time.length() - 1));
            char unit = time.charAt(time.length() - 1);
            return switch (unit) {
                case 's' -> num * 1000L;
                case 'm' -> num * 60 * 1000L;
                case 'h' -> num * 60 * 60 * 1000L;
                case 'd' -> num * 24 * 60 * 60 * 1000L;
                default -> -2;
            };
        } catch (Exception e) {
            return -2;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("serveressentials.command.ban")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .toList();
        }

        if (args.length == 2) {
            return Arrays.asList("30s", "1m", "5m", "1h", "1d", "perm").stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .toList();
        }

        if (args.length == 3) {
            return Arrays.asList("Griefing", "Abuse", "Cheating", "Spamming").stream()
                    .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                    .toList();
        }

        return Collections.emptyList();
    }
}