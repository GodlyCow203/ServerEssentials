

package serveressentials.serveressentials.Player;

import org.bukkit.World;
import org.bukkit.command.*;
        import org.bukkit.entity.Player;
import serveressentials.serveressentials.util.PlayerMessages;

import java.util.ArrayList;
import java.util.List;

public class WeatherCommand implements CommandExecutor, TabCompleter {

    private final PlayerMessages messages;

    public WeatherCommand(PlayerMessages messages) {
        this.messages = messages;

        // Add default messages (only sets if missing)
        messages.addDefault("Weather.Messages.PlayerOnly", "<red>Only players can use this command.");
        messages.addDefault("Weather.Messages.NoPermission", "<red>You do not have permission to use this command.");
        messages.addDefault("Weather.Messages.Usage", "<red>Usage: /weather <clear|rain|thunderstorm>");
        messages.addDefault("Weather.Messages.Clear", "<green>Weather set to clear.");
        messages.addDefault("Weather.Messages.Rain", "<green>Weather set to rain.");
        messages.addDefault("Weather.Messages.Thunderstorm", "<green>Weather set to thunderstorm.");
        messages.addDefault("Weather.Messages.InvalidType", "<red>Invalid weather type. Use: clear, rain, or thunderstorm.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("Weather.Messages.PlayerOnly"));
            return true;
        }

        if (!player.hasPermission("serveressentials.weather")) {
            player.sendMessage(messages.get("Weather.Messages.NoPermission"));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(messages.get("Weather.Messages.Usage"));
            return true;
        }

        World world = player.getWorld();
        String type = args[0].toLowerCase();

        switch (type) {
            case "clear" -> {
                if (!player.hasPermission("serveressentials.weather.clear") &&
                        !player.hasPermission("serveressentials.weather.*")) {
                    player.sendMessage(messages.get("Weather.Messages.NoPermission"));
                    return true;
                }
                world.setStorm(false);
                world.setThundering(false);
                player.sendMessage(messages.get("Weather.Messages.Clear"));
            }
            case "rain" -> {
                if (!player.hasPermission("serveressentials.weather.rain") &&
                        !player.hasPermission("serveressentials.weather.*")) {
                    player.sendMessage(messages.get("Weather.Messages.NoPermission"));
                    return true;
                }
                world.setStorm(true);
                world.setThundering(false);
                player.sendMessage(messages.get("Weather.Messages.Rain"));
            }
            case "thunderstorm" -> {
                if (!player.hasPermission("serveressentials.weather.thunderstorm") &&
                        !player.hasPermission("serveressentials.weather.*")) {
                    player.sendMessage(messages.get("Weather.Messages.NoPermission"));
                    return true;
                }
                world.setStorm(true);
                world.setThundering(true);
                player.sendMessage(messages.get("Weather.Messages.Thunderstorm"));
            }
            default -> player.sendMessage(messages.get("Weather.Messages.InvalidType"));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) return null;

        if (args.length == 1) {
            List<String> options = new ArrayList<>();

            if (player.hasPermission("serveressentials.weather.clear") || player.hasPermission("serveressentials.weather.*")) {
                options.add("clear");
            }
            if (player.hasPermission("serveressentials.weather.rain") || player.hasPermission("serveressentials.weather.*")) {
                options.add("rain");
            }
            if (player.hasPermission("serveressentials.weather.thunderstorm") || player.hasPermission("serveressentials.weather.*")) {
                options.add("thunderstorm");
            }

            String current = args[0].toLowerCase();
            List<String> filtered = new ArrayList<>();
            for (String option : options) if (option.startsWith(current)) filtered.add(option);

            return filtered;
        }

        return null;
    }
}

