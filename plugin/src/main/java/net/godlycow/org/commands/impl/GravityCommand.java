package net.godlycow.org.commands.impl;

import net.kyori.adventure.text.Component;
import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.commands.config.GravityConfig;
import net.godlycow.org.language.LanguageManager;
import net.godlycow.org.language.PlayerLanguageManager;
import net.godlycow.org.language.LanguageManager.ComponentPlaceholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class GravityCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "essc.command.gravity";
    private static final String PERMISSION_OTHERS = "essc.command.gravity.others";
    private final PlayerLanguageManager langManager;
    private final GravityConfig config;
    private final CommandDataStorage dataStorage;

    public GravityCommand(PlayerLanguageManager langManager, GravityConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 2) {
            Component message = langManager.getMessageFor(sender instanceof Player ? (Player) sender : null,
                    "commands.gravity.usage",
                    "<red>Usage: <white>/gravity [player] [-s]");
            sender.sendMessage(message.toString());
            return true;
        }

        boolean silent = false;
        String targetName = null;
        for (String arg : args) {
            if ("-s".equalsIgnoreCase(arg)) {
                silent = true;
            } else if (targetName == null) {
                targetName = arg;
            }
        }

        Player target;
        boolean isSelf = false;

        if (targetName == null) {
            if (!(sender instanceof Player player)) {
                Component message = langManager.getMessageFor(null, "commands.gravity.only-player",
                        "<red>Only players can use this command.");
                sender.sendMessage(message.toString());
                return true;
            }
            target = player;
            isSelf = true;
        } else {
            target = Bukkit.getPlayer(targetName);
            if (target == null || !target.isOnline()) {
                Component message = langManager.getMessageFor(sender instanceof Player ? (Player) sender : null,
                        "commands.gravity.target-not-found",
                        "<red>Player not found.",
                        LanguageManager.ComponentPlaceholder.of("{target}", targetName));
                sender.sendMessage(message.toString());
                return true;
            }
        }

        if (isSelf) {
            if (!sender.hasPermission(PERMISSION)) {
                Component message = langManager.getMessageFor(target, "commands.gravity.no-permission",
                        "<red>You need permission <yellow>{permission}</yellow>!",
                        ComponentPlaceholder.of("{permission}", PERMISSION));
                target.sendMessage(message);
                return true;
            }
        } else {
            if (!sender.hasPermission(PERMISSION_OTHERS)) {
                Component message = langManager.getMessageFor(sender instanceof Player ? (Player) sender : null,
                        "commands.gravity.no-permission-sub",
                        "<red>You need permission <yellow>{subpermission}</yellow>!",
                        LanguageManager.ComponentPlaceholder.of("{subpermission}", PERMISSION_OTHERS));
                sender.sendMessage(message.toString());
                return true;
            }
        }

        boolean newState = !target.hasGravity();
        target.setGravity(newState);

        if (!silent) {
            String stateStr = newState ? "enabled" : "disabled";

            if (isSelf) {
                Component message = langManager.getMessageFor(target, "commands.gravity.toggled-self",
                        "<green>Gravity <white>{state}<green>.",
                        ComponentPlaceholder.of("{state}", stateStr));
                target.sendMessage(message);
            } else {
                Component message = langManager.getMessageFor(
                        sender instanceof Player ? (Player) sender : null,
                        "commands.gravity.toggled-other",
                        "<green>Gravity <white>{state}<green> for <white>{target}<green>.",
                        ComponentPlaceholder.of("{state}", stateStr),
                        ComponentPlaceholder.of("{target}", target.getName()));
                sender.sendMessage(message.toString());
            }
        }

        trackUsage(target.getUniqueId(), newState ? "enabled" : "disabled", 1);

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(prefix))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && "-s".startsWith(args[1].toLowerCase())) {
            return List.of("-s");
        }
        return List.of();
    }

    private void trackUsage(UUID playerId, String type, int value) {
        dataStorage.getState(playerId, "gravity", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "gravity", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "gravity", "last_type", type);
            dataStorage.setState(playerId, "gravity", "last_used", String.valueOf(System.currentTimeMillis()));
        });
    }
}