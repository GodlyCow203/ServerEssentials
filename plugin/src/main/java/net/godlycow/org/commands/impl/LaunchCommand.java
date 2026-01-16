package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.commands.config.LaunchConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.UUID;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class LaunchCommand implements CommandExecutor {
    private static final String PERMISSION = "essc.command.launch";

    private final PlayerLanguageManager langManager;
    private final LaunchConfig config;
    private final CommandDataStorage dataStorage;

    public LaunchCommand(PlayerLanguageManager langManager, LaunchConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.launch.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.launch.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        Vector direction = player.getLocation().getDirection();
        Vector launchVelocity = direction.multiply(config.launchPower()).setY(config.launchHeight());

        player.setVelocity(launchVelocity);
        player.sendMessage(langManager.getMessageFor(player, "commands.launch.launched",
                "<green><bold>WOOSH!</bold> <white>You've been launched!"));

        UUID playerId = player.getUniqueId();
        dataStorage.getState(playerId, "launch", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "launch", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "launch", "last_used", String.valueOf(System.currentTimeMillis()));
        });

        return true;
    }
}