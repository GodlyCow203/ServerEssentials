package net.lunark.io.commands.impl;

import net.lunark.io.commands.CommandDataStorage;
import net.lunark.io.commands.config.WeatherConfig;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.lunark.io.language.LanguageManager.ComponentPlaceholder;

public final class WeatherCommand implements CommandExecutor, TabCompleter {
    private static final String PERMISSION = "serveressentials.command.weather";
    private static final String PERMISSION_CLEAR = "serveressentials.command.weather.clear";
    private static final String PERMISSION_RAIN = "serveressentials.command.weather.rain";
    private static final String PERMISSION_THUNDER = "serveressentials.command.weather.thunderstorm";

    private final PlayerLanguageManager langManager;
    private final WeatherConfig config;
    private final CommandDataStorage dataStorage;

    public WeatherCommand(PlayerLanguageManager langManager, WeatherConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.weather.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.weather.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(langManager.getMessageFor(player, "commands.weather.usage",
                    "<red>Usage: <white>/weather <clear|rain|thunderstorm>"));
            return true;
        }

        World world = player.getWorld();
        String type = args[0].toLowerCase();

        switch (type) {
            case "clear" -> {
                if (!player.hasPermission(PERMISSION_CLEAR)) {
                    player.sendMessage(langManager.getMessageFor(player, "commands.weather.no-permission-sub",
                            "<red>You need permission <yellow>{subpermission}</yellow>!",
                            ComponentPlaceholder.of("{subpermission}", PERMISSION_CLEAR)));
                    return true;
                }
                world.setStorm(false);
                world.setThundering(false);
                world.setWeatherDuration(config.durationTicks());
                player.sendMessage(langManager.getMessageFor(player, "commands.weather.clear",
                        "<green>Weather set to clear for <white>{duration} <green>seconds.",
                        ComponentPlaceholder.of("{duration}", config.durationTicks() / 20)));
            }
            case "rain" -> {
                if (!player.hasPermission(PERMISSION_RAIN)) {
                    player.sendMessage(langManager.getMessageFor(player, "commands.weather.no-permission-sub",
                            "<red>You need permission <yellow>{subpermission}</yellow>!",
                            ComponentPlaceholder.of("{subpermission}", PERMISSION_RAIN)));
                    return true;
                }
                world.setStorm(true);
                world.setThundering(false);
                world.setWeatherDuration(config.durationTicks());
                player.sendMessage(langManager.getMessageFor(player, "commands.weather.rain",
                        "<green>Weather set to rain for <white>{duration} <green>seconds.",
                        ComponentPlaceholder.of("{duration}", config.durationTicks() / 20)));
            }
            case "thunderstorm" -> {
                if (!player.hasPermission(PERMISSION_THUNDER)) {
                    player.sendMessage(langManager.getMessageFor(player, "commands.weather.no-permission-sub",
                            "<red>You need permission <yellow>{subpermission}</yellow>!",
                            ComponentPlaceholder.of("{subpermission}", PERMISSION_THUNDER)));
                    return true;
                }
                world.setStorm(true);
                world.setThundering(true);
                world.setWeatherDuration(config.durationTicks());
                player.sendMessage(langManager.getMessageFor(player, "commands.weather.thunderstorm",
                        "<green>Weather set to thunderstorm for <white>{duration} <green>seconds.",
                        ComponentPlaceholder.of("{duration}", config.durationTicks() / 20)));
            }
            default -> {
                player.sendMessage(langManager.getMessageFor(player, "commands.weather.invalid-type",
                        "<red>Invalid weather type. Use: clear, rain, or thunderstorm."));
                return true;
            }
        }

        // Store usage statistics (async)
        UUID playerId = player.getUniqueId();
        dataStorage.setState(playerId, "weather", "last_type", type);
        dataStorage.getState(playerId, "weather", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "weather", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "weather", "last_used", String.valueOf(System.currentTimeMillis()));
        });

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) return List.of();

        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            String partial = args[0].toLowerCase();

            if (player.hasPermission(PERMISSION_CLEAR) && "clear".startsWith(partial)) {
                options.add("clear");
            }
            if (player.hasPermission(PERMISSION_RAIN) && "rain".startsWith(partial)) {
                options.add("rain");
            }
            if (player.hasPermission(PERMISSION_THUNDER) && "thunderstorm".startsWith(partial)) {
                options.add("thunderstorm");
            }

            return options;
        }

        return List.of();
    }
}