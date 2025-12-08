package net.lunark.io.commands.impl;

import net.lunark.io.commands.config.ScoreboardConfig;
import net.lunark.io.scoreboard.ScoreboardStorage;
import net.lunark.io.scoreboard.ScoreboardUpdater;
import net.lunark.io.language.PlayerLanguageManager;
import net.lunark.io.language.LanguageManager.ComponentPlaceholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class ScoreboardCommand implements CommandExecutor, TabCompleter {
    private static final String PERMISSION_BASE = "serveressentials.command.scoreboard";
    private static final String PERMISSION_RELOAD = "serveressentials.command.scoreboard.reload";
    private static final String PERMISSION_TOGGLE = "serveressentials.command.scoreboard.toggle";
    private static final String PERMISSION_COLOR = "serveressentials.command.scoreboard.color";

    private final PlayerLanguageManager langManager;
    private final ScoreboardConfig config;
    private final ScoreboardStorage storage;
    private final ScoreboardUpdater updater;
    private final Plugin plugin;

    public ScoreboardCommand(Plugin plugin, PlayerLanguageManager langManager, ScoreboardConfig config,
                             ScoreboardStorage storage, ScoreboardUpdater updater) {
        this.langManager = langManager;
        this.config = config;
        this.storage = storage;
        this.updater = updater;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.scoreboard.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!config.enabled) {
            player.sendMessage(langManager.getMessageFor(player, "commands.scoreboard.disabled",
                    "<red>Scoreboard system is disabled!"));
            return true;
        }

        if (!player.hasPermission(PERMISSION_BASE)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.scoreboard.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION_BASE)));
            return true;
        }

        if (args.length == 0) {
            showUsage(player);
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "reload":
                handleReload(player);
                break;
            case "toggle":
                handleToggle(player);
                break;
            case "color":
                handleColor(player, args);
                break;
            default:
                showUsage(player);
        }

        return true;
    }

    private void showUsage(Player player) {
        player.sendMessage(langManager.getMessageFor(player, "commands.scoreboard.usage",
                "<yellow>Usage:</yellow> /scoreboard <reload|toggle|color> [layout]"));
    }

    private void handleReload(Player player) {
        if (!player.hasPermission(PERMISSION_RELOAD)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.scoreboard.no-permission-reload",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION_RELOAD)));
            return;
        }

        updater.refreshAll();

        player.sendMessage(langManager.getMessageFor(player, "commands.scoreboard.reload.success",
                "<green>Scoreboards reloaded successfully!"));
    }

    private void handleToggle(Player player) {
        if (!player.hasPermission(PERMISSION_TOGGLE)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.scoreboard.no-permission-toggle",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION_TOGGLE)));
            return;
        }

        UUID uuid = player.getUniqueId();
        boolean current = storage.isEnabled(uuid);
        boolean newState = !current;

        storage.setEnabled(uuid, newState).thenAccept(v -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (newState) {
                    updater.update(player);
                    player.sendMessage(langManager.getMessageFor(player, "commands.scoreboard.toggle.enabled",
                            "<green>Scoreboard enabled!"));
                } else {
                    updater.clear(player);
                    player.sendMessage(langManager.getMessageFor(player, "commands.scoreboard.toggle.disabled",
                            "<gray>Scoreboard disabled!"));
                }
            });
        });
    }

    private void handleColor(Player player, String[] args) {
        if (!player.hasPermission(PERMISSION_COLOR)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.scoreboard.no-permission-color",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION_COLOR)));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(langManager.getMessageFor(player, "commands.scoreboard.color.usage",
                    "<red>Usage: /scoreboard color <layout>"));
            return;
        }

        String layout = args[1].toLowerCase();
        if (!config.layoutExists(layout)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.scoreboard.color.not-found",
                    "<red>Layout '<yellow>{layout}' not found!",
                    ComponentPlaceholder.of("{layout}", layout)));
            return;
        }

        UUID uuid = player.getUniqueId();
        storage.setLayout(uuid, layout).thenAccept(v -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                updater.update(player, layout);
                player.sendMessage(langManager.getMessageFor(player, "commands.scoreboard.color.changed",
                        "<green>Layout changed to <yellow>{layout}</yellow>!",
                        ComponentPlaceholder.of("{layout}", layout)));
            });
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) return List.of();

        if (!player.hasPermission(PERMISSION_BASE)) return List.of();

        if (!config.enabled) return List.of();

        if (args.length == 1) {
            return Arrays.asList("reload", "toggle", "color").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("color")) {
            return config.getLayoutNames().stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return List.of();
    }
}