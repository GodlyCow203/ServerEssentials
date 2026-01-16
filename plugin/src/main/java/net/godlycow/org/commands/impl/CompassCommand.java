package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.commands.config.CompassConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class CompassCommand implements CommandExecutor {
    private static final String PERMISSION = "essc.command.compass";

    private final PlayerLanguageManager langManager;
    private final CompassConfig config;
    private final CommandDataStorage dataStorage;

    public CompassCommand(PlayerLanguageManager langManager, CompassConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.compass.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.compass.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        float yaw = player.getLocation().getYaw();

        if (yaw < 0) yaw += 360;

        String direction;
        if (yaw >= 337.5 || yaw < 22.5) direction = "North";
        else if (yaw < 67.5) direction = "North-East";
        else if (yaw < 112.5) direction = "East";
        else if (yaw < 157.5) direction = "South-East";
        else if (yaw < 202.5) direction = "South";
        else if (yaw < 247.5) direction = "South-West";
        else if (yaw < 292.5) direction = "West";
        else direction = "North-West";

        player.sendMessage(langManager.getMessageFor(player, "commands.compass.facing",
                "<green>You are facing <gold><bold>{direction}",
                ComponentPlaceholder.of("{direction}", direction)));

        UUID playerId = player.getUniqueId();
        dataStorage.getState(playerId, "compass", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "compass", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "compass", "last_direction", direction);
        });

        return true;
    }
}