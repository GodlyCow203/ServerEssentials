package net.godlycow.org.commands.impl;

import net.kyori.adventure.text.Component;
import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.commands.config.SleepConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class SleepCommand implements CommandExecutor {
    private static final String PERMISSION = "essc.command.sleep";

    private final PlayerLanguageManager langManager;
    private final SleepConfig config;
    private final CommandDataStorage dataStorage;

    public SleepCommand(PlayerLanguageManager langManager, SleepConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.sleep.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.sleep.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        World world = player.getWorld();

        world.setTime(config.timeToSet());

        if (config.clearWeather()) {
            world.setStorm(false);
            world.setThundering(false);
            world.setWeatherDuration(config.weatherDuration());
        }

        player.sendMessage(langManager.getMessageFor(player, "commands.sleep.success",
                "<green>The night has been skipped and the weather cleared!"));

        if (config.broadcastMessage()) {
            Component broadcastMsg = langManager.getMessageFor(null, "commands.sleep.broadcast",
                    "<yellow>{player} <green>skipped the night! <gray>(Sleep command)",
                    ComponentPlaceholder.of("{player}", player.getName()));

            Bukkit.broadcast(broadcastMsg);
        }

        UUID playerId = player.getUniqueId();
        dataStorage.getState(playerId, "sleep", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "sleep", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "sleep", "last_world", world.getName());
        });

        return true;
    }
}