package net.godlycow.org.commands.impl;

import net.kyori.adventure.text.Component;
import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.commands.config.WeatherConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

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
                    "<#B22222>❌ Only players can use this command."));
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.weather.no-permission",
                    "<#B22222>❌ You need permission <#FFD900>{permission}</#FFD900>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        if (args.length == 0 || args.length > 1 || args[0].equalsIgnoreCase("help")) {
            sendHelpMessage(player);
            return true;
        }

        World world = player.getWorld();
        String type = args[0].toLowerCase();

        switch (type) {
            case "clear" -> {
                if (!player.hasPermission(PERMISSION_CLEAR)) {
                    player.sendMessage(langManager.getMessageFor(player, "commands.weather.no-permission-sub",
                            "<#B22222>❌ You need permission <#FFD900>{subpermission}</#FFD900>!",
                            ComponentPlaceholder.of("{subpermission}", PERMISSION_CLEAR)));
                    return true;
                }
                world.setStorm(false);
                world.setThundering(false);
                world.setWeatherDuration(config.durationTicks());
                player.sendMessage(langManager.getMessageFor(player, "commands.weather.clear",
                        "<#3BB302>✔ Weather set to clear for <white>{duration} <#3BB302>✔ seconds.",
                        ComponentPlaceholder.of("{duration}", config.durationTicks() / 20)));
            }
            case "rain" -> {
                if (!player.hasPermission(PERMISSION_RAIN)) {
                    player.sendMessage(langManager.getMessageFor(player, "commands.weather.no-permission-sub",
                            "<#B22222>❌ You need permission <#FFD900>{subpermission}</#FFD900>!",
                            ComponentPlaceholder.of("{subpermission}", PERMISSION_RAIN)));
                    return true;
                }
                world.setStorm(true);
                world.setThundering(false);
                world.setWeatherDuration(config.durationTicks());
                player.sendMessage(langManager.getMessageFor(player, "commands.weather.rain",
                        "<#1E90FF>Weather set to rain for <white>{duration} <#1E90FF>seconds.",
                        ComponentPlaceholder.of("{duration}", config.durationTicks() / 20)));
            }
            case "thunderstorm" -> {
                if (!player.hasPermission(PERMISSION_THUNDER)) {
                    player.sendMessage(langManager.getMessageFor(player, "commands.weather.no-permission-sub",
                            "<#B22222>❌ You need permission <#FFD900>{subpermission}</#FFD900>!",
                            ComponentPlaceholder.of("{subpermission}", PERMISSION_THUNDER)));
                    return true;
                }
                world.setStorm(true);
                world.setThundering(true);
                world.setWeatherDuration(config.durationTicks());
                player.sendMessage(langManager.getMessageFor(player, "commands.weather.thunderstorm",
                        "<#FFD900>Weather set to thunderstorm for <white>{duration} <#FFD900>seconds.",
                        ComponentPlaceholder.of("{duration}", config.durationTicks() / 20)));
            }
            default -> {
                player.sendMessage(langManager.getMessageFor(player, "commands.weather.invalid-type",
                        "<#B22222>❌ Invalid weather type. Use: clear, rain, or thunderstorm."));
                sendHelpMessage(player);
                return true;
            }
        }

        UUID playerId = player.getUniqueId();
        dataStorage.setState(playerId, "weather", "last_type", type);
        dataStorage.getState(playerId, "weather", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "weather", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "weather", "last_used", String.valueOf(System.currentTimeMillis()));
        });

        return true;
    }



    private void sendHelpMessage(Player player) {
        Component help = Component.empty()

                // HEADER
                .append(langManager.getMessageFor(player, "commands.weather.help.header",
                        "<#FFD900><bold>=== Weather Command Help ===</bold></#FFD900>"))
                .append(Component.newline())

                // DESCRIPTION
                .append(langManager.getMessageFor(player, "commands.weather.help.description",
                        "<gray>Change the weather in your current world for a set duration.</gray>"))
                .append(Component.newline())

                // USAGE
                .append(langManager.getMessageFor(player, "commands.weather.help.usage",
                        "<#FFD900>Usage:</#FFD900> <white>/weather <type></white>"))
                .append(Component.newline())

                // TYPES HEADER
                .append(langManager.getMessageFor(player, "commands.weather.help.types.header",
                        "<#FFD900>Available Weather Types:</#FFD900>"))
                .append(Component.newline());

        // CLEAR
        help = help.append(
                player.hasPermission(PERMISSION_CLEAR)
                        ? langManager.getMessageFor(player, "commands.weather.help.types.clear",
                        "  <white>• clear</white> <gray>- Set weather to sunny</gray>")
                        : langManager.getMessageFor(player, "commands.weather.help.types.clear.no-perm",
                        "  <dark_gray>• clear</dark_gray> <gray>- No permission</gray>")
        ).append(Component.newline());

        // RAIN
        help = help.append(
                player.hasPermission(PERMISSION_RAIN)
                        ? langManager.getMessageFor(player, "commands.weather.help.types.rain",
                        "  <white>• rain</white> <gray>- Set weather to rain</gray>")
                        : langManager.getMessageFor(player, "commands.weather.help.types.rain.no-perm",
                        "  <dark_gray>• rain</dark_gray> <gray>- No permission</gray>")
        ).append(Component.newline());

        // THUNDER
        help = help.append(
                player.hasPermission(PERMISSION_THUNDER)
                        ? langManager.getMessageFor(player, "commands.weather.help.types.thunderstorm",
                        "  <white>• thunderstorm</white> <gray>- Set weather to thunderstorm</gray>")
                        : langManager.getMessageFor(player, "commands.weather.help.types.thunderstorm.no-perm",
                        "  <dark_gray>• thunderstorm</dark_gray> <gray>- No permission</gray>")
        ).append(Component.newline());

        // DURATION
        help = help.append(langManager.getMessageFor(player, "commands.weather.help.duration",
                        "<#FFD900>Duration:</#FFD900> <white>{duration} seconds</white>",
                        ComponentPlaceholder.of("{duration}", config.durationTicks() / 20)))
                .append(Component.newline());

        // FOOTER
        help = help.append(langManager.getMessageFor(player, "commands.weather.help.footer",
                "<dark_gray>Use /weather help to see this message again.</dark_gray>"));

        player.sendMessage(help);
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) return List.of();

        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            String partial = args[0].toLowerCase();

            if ("help".startsWith(partial)) options.add("help");
            if (player.hasPermission(PERMISSION_CLEAR) && "clear".startsWith(partial)) options.add("clear");
            if (player.hasPermission(PERMISSION_RAIN) && "rain".startsWith(partial)) options.add("rain");
            if (player.hasPermission(PERMISSION_THUNDER) && "thunderstorm".startsWith(partial)) options.add("thunderstorm");

            return options;
        }

        return List.of();
    }
}