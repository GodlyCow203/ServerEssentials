package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.commands.config.PingConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class PingCommand implements CommandExecutor {
    private static final String PERMISSION = "essc.command.ping";

    private final PlayerLanguageManager langManager;
    private final PingConfig config;
    private final CommandDataStorage dataStorage;

    public PingCommand(PlayerLanguageManager langManager, PingConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.ping.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.ping.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        int ping = player.getPing();

        String pingColor;
        if (ping < 50) {
            pingColor = "<green>";
        } else if (ping < 100) {
            pingColor = "<yellow>";
        } else if (ping < 200) {
            pingColor = "<gold>";
        } else {
            pingColor = "<red>";
        }

        player.sendMessage(langManager.getMessageFor(player, "commands.ping.show",
                "<gray>Your ping: " + pingColor + "{ping}ms",
                ComponentPlaceholder.of("{ping}", ping)));

        UUID playerId = player.getUniqueId();
        dataStorage.getState(playerId, "ping", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "ping", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "ping", "last_ping", String.valueOf(ping));
            dataStorage.setState(playerId, "ping", "last_checked", String.valueOf(System.currentTimeMillis()));
        });

        return true;
    }
}