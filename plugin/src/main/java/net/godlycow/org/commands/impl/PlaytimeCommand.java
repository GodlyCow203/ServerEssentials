package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.commands.config.PlaytimeConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class PlaytimeCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.playtime";

    private final PlayerLanguageManager langManager;
    private final PlaytimeConfig config;
    private final CommandDataStorage dataStorage;

    public PlaytimeCommand(PlayerLanguageManager langManager, PlaytimeConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.playtime.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.playtime.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        if (args.length > 0) {
            player.sendMessage(langManager.getMessageFor(player, "commands.playtime.usage",
                    "<red>Usage: <white>/playtime"));
            return true;
        }

        long ticks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        long minutes = ticks / (20 * 60);
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;

        player.sendMessage(langManager.getMessageFor(player, "commands.playtime.playtime",
                "<green>Your playtime: <gold>{hours}h {minutes}m",
                ComponentPlaceholder.of("{hours}", hours),
                ComponentPlaceholder.of("{minutes}", remainingMinutes)));

        UUID playerId = player.getUniqueId();
        dataStorage.getState(playerId, "playtime", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "playtime", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "playtime", "last_checked", String.valueOf(System.currentTimeMillis()));
        });

        return true;
    }
}