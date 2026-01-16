package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.commands.config.SeenConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class SeenCommand implements CommandExecutor {
    private static final String PERMISSION = "essc.command.seen";

    private final PlayerLanguageManager langManager;
    private final SeenConfig config;
    private final CommandDataStorage dataStorage;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public SeenCommand(PlayerLanguageManager langManager, SeenConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.seen.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.seen.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(langManager.getMessageFor(player, "commands.seen.usage",
                    "<yellow>Usage: <white>/seen <player>"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(args[0]);
        if (target == null) {

            OfflinePlayer exact = Bukkit.getOfflinePlayer(args[0]);
            if (exact != null && (exact.hasPlayedBefore() || exact.isOnline())) {
                target = exact;
            }
        }

        if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
            player.sendMessage(langManager.getMessageFor(player, "commands.seen.player-not-found",
                    "<red>Player <yellow>{player}</yellow> not found.",
                    ComponentPlaceholder.of("{player}", args[0])));
            return true;
        }

        String playerName = target.getName() != null ? target.getName() : args[0];

        if (target.getFirstPlayed() == 0) {
            player.sendMessage(langManager.getMessageFor(player, "commands.seen.never-joined",
                    "<yellow>{player} <yellow>has never joined the server.",
                    ComponentPlaceholder.of("{player}", playerName)));
            return true;
        }

        String lastSeen = dateFormat.format(new Date(target.getLastPlayed()));

        if (target.isOnline()) {
            player.sendMessage(langManager.getMessageFor(player, "commands.seen.result-online",
                    "<green>{player} <green>is currently <bold>online</bold>!",
                    ComponentPlaceholder.of("{player}", playerName)));
        } else {
            player.sendMessage(langManager.getMessageFor(player, "commands.seen.result",
                    "<green>{player} <green>was last seen on <white>{lastSeen}",
                    ComponentPlaceholder.of("{player}", playerName),
                    ComponentPlaceholder.of("{lastSeen}", lastSeen)));
        }

        UUID playerId = player.getUniqueId();
        OfflinePlayer finalTarget = target;
        dataStorage.getState(playerId, "seen", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "seen", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "seen", "last_target", finalTarget.getUniqueId().toString());
        });

        return true;
    }
}