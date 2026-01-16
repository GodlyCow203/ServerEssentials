package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.commands.config.SuicideConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class SuicideCommand implements CommandExecutor {
    private static final String PERMISSION = "essc.command.suicide";

    private final PlayerLanguageManager langManager;
    private final SuicideConfig config;
    private final CommandDataStorage dataStorage;

    public SuicideCommand(PlayerLanguageManager langManager, SuicideConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.suicide.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.suicide.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        UUID playerId = player.getUniqueId();
        dataStorage.getState(playerId, "suicide", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "suicide", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "suicide", "last_used", String.valueOf(System.currentTimeMillis()));
        }).exceptionally(ex -> {
            player.sendMessage(langManager.getMessageFor(player, "commands.suicide.error",
                    "<red>Failed to record suicide attempt."));
            return null;
        });

        player.setHealth(0);
        player.sendMessage(langManager.getMessageFor(player, "commands.suicide.success",
                "<dark_red>You have taken your own life."));

        return true;
    }
}