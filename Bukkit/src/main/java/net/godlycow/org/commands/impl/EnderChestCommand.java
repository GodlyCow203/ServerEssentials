package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.commands.config.EnderChestConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class EnderChestCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.enderchest";
    private static final String PERMISSION_OTHERS = "serveressentials.command.enderchest.others";

    private final PlayerLanguageManager langManager;
    private final EnderChestConfig config;
    private final CommandDataStorage dataStorage;

    public EnderChestCommand(PlayerLanguageManager langManager, EnderChestConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.enderchest.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.enderchest.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(langManager.getMessageFor(player, "commands.enderchest.opening",
                    "<green>Opening your ender chest..."));
            player.openInventory(player.getEnderChest());

            trackUsage(player.getUniqueId(), "self");
            return true;
        }

        if (args.length == 1) {
            if (!player.hasPermission(PERMISSION_OTHERS)) {
                player.sendMessage(langManager.getMessageFor(player, "commands.enderchest.no-permission-others",
                        "<red>You need permission <yellow>{permission}</yellow>!",
                        ComponentPlaceholder.of("{permission}", PERMISSION_OTHERS)));
                return true;
            }

            Player target = Bukkit.getPlayerExact(args[0]);
            if (target != null && target.isOnline()) {
                player.sendMessage(langManager.getMessageFor(player, "commands.enderchest.opening-other",
                        "<green>Opening <yellow>{player}'s <green>ender chest...",
                        ComponentPlaceholder.of("{player}", target.getName())));
                player.openInventory(target.getEnderChest());

                trackUsage(player.getUniqueId(), "other");
                trackTargetUsage(target.getUniqueId());
                return true;
            }

            OfflinePlayer offline = Bukkit.getOfflinePlayer(args[0]);
            if (offline.hasPlayedBefore()) {
                player.sendMessage(langManager.getMessageFor(player, "commands.enderchest.offline-not-supported",
                        "<red>Cannot open ender chest of offline player <yellow>{player}</yellow>.",
                        ComponentPlaceholder.of("{player}", args[0])));
            } else {
                player.sendMessage(langManager.getMessageFor(player, "commands.enderchest.player-not-found",
                        "<red>Player <yellow>{player}</yellow> not found.",
                        ComponentPlaceholder.of("{player}", args[0])));
            }
            return true;
        }

        player.sendMessage(langManager.getMessageFor(player, "commands.enderchest.usage",
                "<red>Usage: <white>/enderchest [player]"));
        return true;
    }

    private void trackUsage(UUID playerId, String type) {
        dataStorage.getState(playerId, "enderchest", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "enderchest", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "enderchest", "last_type", type);
            dataStorage.setState(playerId, "enderchest", "last_used", String.valueOf(System.currentTimeMillis()));
        });
    }

    private void trackTargetUsage(UUID targetId) {
        dataStorage.getState(targetId, "enderchest", "accessed_by_others").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(targetId, "enderchest", "accessed_by_others", String.valueOf(count + 1));
        });
    }
}