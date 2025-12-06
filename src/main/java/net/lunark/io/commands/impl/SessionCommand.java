package net.lunark.io.commands.impl;

import net.lunark.io.Managers.SessionManager;
import net.lunark.io.commands.CommandDataStorage;
import net.lunark.io.commands.config.SessionConfig;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

import static net.lunark.io.language.LanguageManager.ComponentPlaceholder;

public final class SessionCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.session";

    private final PlayerLanguageManager langManager;
    private final SessionConfig config;
    private final SessionManager sessionManager;
    private final CommandDataStorage dataStorage;

    public SessionCommand(PlayerLanguageManager langManager, SessionConfig config, SessionManager sessionManager, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.sessionManager = sessionManager;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.session.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.session.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        long current = sessionManager.getCurrentSession(player);
        if (current == 0) {
            player.sendMessage(langManager.getMessageFor(player, "commands.session.not-tracked",
                    "<yellow>Your session is not being tracked.<gray> (Rejoin to start tracking)"));
            return true;
        }

        sessionManager.getLongestSession(player).thenAccept(longest -> {
            String currentStr = formatDuration(current);
            String longestStr = formatDuration(longest);

            player.sendMessage(langManager.getMessageFor(player, "commands.session.info",
                    "<green>Session Info:\n" +
                            "<gray>Current: <white>{current}\n" +
                            "<gray>Longest: <white>{longest}",
                    ComponentPlaceholder.of("{current}", currentStr),
                    ComponentPlaceholder.of("{longest}", longestStr)));

            UUID playerId = player.getUniqueId();
            dataStorage.getState(playerId, "session", "usage_count").thenAccept(opt -> {
                int count = opt.map(Integer::parseInt).orElse(0);
                dataStorage.setState(playerId, "session", "usage_count", String.valueOf(count + 1));
                dataStorage.setState(playerId, "session", "last_checked", String.valueOf(System.currentTimeMillis()));
            });
        });

        return true;
    }


    private String formatDuration(long millis) {
        long seconds = millis / 1000 % 60;
        long minutes = millis / (1000 * 60) % 60;
        long hours = millis / (1000 * 60 * 60);

        return String.format("%02dh %02dm %02ds", hours, minutes, seconds);
    }
}