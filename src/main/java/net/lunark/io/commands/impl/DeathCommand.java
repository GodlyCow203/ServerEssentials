package net.lunark.io.commands.impl;

import net.lunark.io.commands.CommandDataStorage;
import net.lunark.io.commands.config.DeathConfig;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

import static net.lunark.io.language.LanguageManager.ComponentPlaceholder;

public final class DeathCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.death";

    private final PlayerLanguageManager langManager;
    private final DeathConfig config;
    private final CommandDataStorage dataStorage;

    public DeathCommand(PlayerLanguageManager langManager, DeathConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.death.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.death.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        int deaths = player.getStatistic(Statistic.DEATHS);
        player.sendMessage(langManager.getMessageFor(player, "commands.death.count",
                "<red>You have died <gold>{deaths} <red>times.",
                ComponentPlaceholder.of("{deaths}", deaths)));

        UUID playerId = player.getUniqueId();
        dataStorage.getState(playerId, "death", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "death", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "death", "last_check", String.valueOf(System.currentTimeMillis()));
        });

        return true;
    }
}