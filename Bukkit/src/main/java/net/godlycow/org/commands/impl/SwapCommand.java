package net.godlycow.org.commands.impl;

import net.kyori.adventure.text.Component;
import net.godlycow.org.commands.config.SwapConfig;
import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.language.PlayerLanguageManager;
import net.godlycow.org.language.LanguageManager.ComponentPlaceholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class SwapCommand implements CommandExecutor {

    private static final String PERMISSION = "serveressentials.command.swap";
    private final PlayerLanguageManager langManager;
    private final SwapConfig config;
    private final CommandDataStorage dataStorage;

    public SwapCommand(PlayerLanguageManager langManager, SwapConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            Component message = langManager.getMessageFor(null, "commands.swap.only-player", "<red>Only players can use this command!</red>");
            sender.sendMessage(message.toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            Component message = langManager.getMessageFor(player, "commands.swap.no-permission", "<red>You need permission <yellow>{permission}</yellow>!</red>", ComponentPlaceholder.of("{permission}", PERMISSION));
            player.sendMessage(message);
            return true;
        }

        if (args.length != 1) {
            Component message = langManager.getMessageFor(player, "commands.swap.usage", "<red>Usage: <white>/swap <player></white></red>");
            player.sendMessage(message);
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || target.equals(player)) {
            Component message = langManager.getMessageFor(player, "commands.swap.target-not-found", "<red>Player <white>{target}</white> not found or cannot swap with yourself.</red>", ComponentPlaceholder.of("{target}", args[0]));
            player.sendMessage(message);
            return true;
        }

        var loc1 = player.getLocation();
        var loc2 = target.getLocation();
        player.teleport(loc2);
        target.teleport(loc1);

        Component swappedMsgPlayer = langManager.getMessageFor(player, "commands.swap.success", "<green>You swapped locations with <white>{target}</white>!</green>", ComponentPlaceholder.of("{target}", target.getName()));
        player.sendMessage(swappedMsgPlayer);

        Component swappedMsgTarget = langManager.getMessageFor(target, "commands.swap.target-notified", "<green><white>{player}</white> swapped locations with you!</green>", ComponentPlaceholder.of("{player}", player.getName()));
        target.sendMessage(swappedMsgTarget);

        trackUsage(player.getUniqueId(), "swap", 1);

        return true;
    }

    private void trackUsage(UUID playerId, String type, int value) {
        dataStorage.getState(playerId, "swap", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "swap", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "swap", "last_type", type);
            dataStorage.setState(playerId, "swap", "last_used", String.valueOf(System.currentTimeMillis()));
        });
    }
}