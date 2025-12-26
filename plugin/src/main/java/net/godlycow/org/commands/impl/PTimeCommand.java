package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.commands.config.PTimeConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class PTimeCommand implements CommandExecutor, TabCompleter {
    private static final String PERMISSION = "serveressentials.command.ptime";
    private static final String PERMISSION_DAY = "serveressentials.command.ptime.day";
    private static final String PERMISSION_NIGHT = "serveressentials.command.ptime.night";
    private static final String PERMISSION_RESET = "serveressentials.command.ptime.reset";

    private final PlayerLanguageManager langManager;
    private final PTimeConfig config;
    private final CommandDataStorage dataStorage;

    public PTimeCommand(PlayerLanguageManager langManager, PTimeConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.ptime.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.ptime.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(langManager.getMessageFor(player, "commands.ptime.usage",
                    "<red>Usage: <white>/ptime <day|night|reset>"));
            return true;
        }

        String option = args[0].toLowerCase();

        switch (option) {
            case "day" -> {
                if (!player.hasPermission(PERMISSION_DAY)) {
                    player.sendMessage(langManager.getMessageFor(player, "commands.ptime.no-permission-sub",
                            "<red>You need permission <yellow>{subpermission}</yellow>!",
                            ComponentPlaceholder.of("{subpermission}", PERMISSION_DAY)));
                    return true;
                }
                player.setPlayerTime(config.dayTime(), false);
            }
            case "night" -> {
                if (!player.hasPermission(PERMISSION_NIGHT)) {
                    player.sendMessage(langManager.getMessageFor(player, "commands.ptime.no-permission-sub",
                            "<red>You need permission <yellow>{subpermission}</yellow>!",
                            ComponentPlaceholder.of("{subpermission}", PERMISSION_NIGHT)));
                    return true;
                }
                player.setPlayerTime(config.nightTime(), false);
            }
            case "reset" -> {
                if (!player.hasPermission(PERMISSION_RESET)) {
                    player.sendMessage(langManager.getMessageFor(player, "commands.ptime.no-permission-sub",
                            "<red>You need permission <yellow>{subpermission}</yellow>!",
                            ComponentPlaceholder.of("{subpermission}", PERMISSION_RESET)));
                    return true;
                }
                player.resetPlayerTime();
            }
            default -> {
                player.sendMessage(langManager.getMessageFor(player, "commands.ptime.invalid-option",
                        "<red>Invalid option! Use: <white>day, night, or reset"));
                return true;
            }
        }

        player.sendMessage(langManager.getMessageFor(player, "commands.ptime.success",
                "<green>Personal time set to <gold>{option}",
                ComponentPlaceholder.of("{option}", option.toUpperCase())));

        UUID playerId = player.getUniqueId();
        dataStorage.getState(playerId, "ptime", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "ptime", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "ptime", "last_option", option);
        });

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) return List.of();

        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            if (player.hasPermission(PERMISSION_DAY) && "day".startsWith(partial)) {
                completions.add("day");
            }
            if (player.hasPermission(PERMISSION_NIGHT) && "night".startsWith(partial)) {
                completions.add("night");
            }
            if (player.hasPermission(PERMISSION_RESET) && "reset".startsWith(partial)) {
                completions.add("reset");
            }
        }
        return completions;
    }
}