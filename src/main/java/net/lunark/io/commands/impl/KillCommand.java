package net.lunark.io.commands.impl;

import net.lunark.io.commands.CommandDataStorage;
import net.lunark.io.commands.config.KillConfig;
import net.lunark.io.language.PlayerLanguageManager;
import net.lunark.io.listeners.KillTracker;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

import static net.lunark.io.language.LanguageManager.ComponentPlaceholder;

public final class KillCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.kills";

    private final PlayerLanguageManager langManager;
    private final KillConfig config;
    private final KillTracker killTracker;
    private final CommandDataStorage dataStorage;

    public KillCommand(PlayerLanguageManager langManager, KillConfig config,
                       KillTracker killTracker, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.killTracker = killTracker;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.kill.only-players",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.kill.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        // Get kills from tracker and sync to database
        UUID playerId = player.getUniqueId();
        int kills = killTracker.getKills(player);

        // Async database update
        dataStorage.setState(playerId, "kill", "kill_count", String.valueOf(kills)).thenAccept(v -> {
            dataStorage.setState(playerId, "kill", "last_check", String.valueOf(System.currentTimeMillis()));
        });

        player.sendMessage(langManager.getMessageFor(player, "commands.kill.kill-count",
                "<green>You have <gold>{kills} <green>player kills.",
                ComponentPlaceholder.of("{kills}", kills)));

        return true;
    }
}