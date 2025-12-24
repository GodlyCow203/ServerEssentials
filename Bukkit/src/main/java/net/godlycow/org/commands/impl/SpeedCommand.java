package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.commands.config.SpeedConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class SpeedCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.speed";

    private final PlayerLanguageManager langManager;
    private final SpeedConfig config;
    private final CommandDataStorage dataStorage;

    public SpeedCommand(PlayerLanguageManager langManager, SpeedConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.speed.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.speed.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(langManager.getMessageFor(player, "commands.speed.usage",
                    "<red>Usage: <white>/speed <1-10>"));
            return true;
        }

        try {
            int speed = Integer.parseInt(args[0]);
            if (speed < 1 || speed > 10) {
                throw new NumberFormatException("Speed must be between 1-10");
            }

            float scaled = speed / 10.0f;

            if (player.isFlying()) {
                player.setFlySpeed(scaled);
                player.sendMessage(langManager.getMessageFor(player, "commands.speed.fly-set",
                        "<green>Fly speed set to <gold>{speed}",
                        ComponentPlaceholder.of("{speed}", speed)));
            } else {
                player.setWalkSpeed(scaled);
                player.sendMessage(langManager.getMessageFor(player, "commands.speed.walk-set",
                        "<green>Walk speed set to <gold>{speed}",
                        ComponentPlaceholder.of("{speed}", speed)));
            }

            trackUsage(player.getUniqueId(), speed, true);

        } catch (NumberFormatException e) {
            player.sendMessage(langManager.getMessageFor(player, "commands.speed.invalid-number",
                    "<red>Invalid number! Speed must be between <yellow>1-10"));

            trackUsage(player.getUniqueId(), 0, false);
        }

        return true;
    }

    private void trackUsage(UUID playerId, int speed, boolean success) {
        dataStorage.getState(playerId, "speed", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "speed", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "speed", "last_speed", String.valueOf(speed));
            dataStorage.setState(playerId, "speed", "last_success", String.valueOf(success));
        });
    }
}