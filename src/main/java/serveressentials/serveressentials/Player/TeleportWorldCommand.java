package serveressentials.serveressentials.Player;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.util.PlayerMessages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TeleportWorldCommand implements CommandExecutor, TabCompleter {

    private final PlayerMessages messages;

    public TeleportWorldCommand(PlayerMessages messages) {
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("tpp.no-permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(messages.get("tpp.usage"));
            return true;
        }

        String targetName = args[0];
        String worldName = args[1];

        if (!player.hasPermission("serveressentials.tpp." + worldName.toLowerCase())) {
            player.sendMessage(messages.get("tpp.no-permission"));
            return true;
        }

        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            player.sendMessage(messages.get("tpp.player-not-found", "<player>", targetName));
            return true;
        }

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            player.sendMessage(messages.get("tpp.world-not-loaded", "<world>", worldName));
            return true;
        }

        Location loc;

        if (args.length == 5) { // x y z provided
            try {
                double x = Double.parseDouble(args[2]);
                double y = Double.parseDouble(args[3]);
                double z = Double.parseDouble(args[4]);
                loc = new Location(world, x, y, z);
            } catch (NumberFormatException e) {
                player.sendMessage(messages.get("tpp.invalid-coords"));
                return true;
            }
        } else {
            loc = world.getSpawnLocation();
        }

        target.teleport(loc);

        sender.sendMessage(messages.get("tpp.teleported-target",
                "<player>", target.getName(),
                "<world>", worldName));

        target.sendMessage(messages.get("tpp.teleported-self",
                "<world>", worldName,
                "<x>", String.valueOf(loc.getX()),
                "<y>", String.valueOf(loc.getY()),
                "<z>", String.valueOf(loc.getZ())));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if (!(sender instanceof Player player)) return Collections.emptyList();

        if (args.length == 1) { // Player names
            String input = args[0].toLowerCase();
            List<String> onlinePlayers = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(input)) onlinePlayers.add(p.getName());
            }
            return onlinePlayers;
        }

        if (args.length == 2) { // World names
            String input = args[1].toLowerCase();
            List<String> allowedWorlds = new ArrayList<>();
            for (World world : Bukkit.getWorlds()) {
                if (player.hasPermission("serveressentials.tpp." + world.getName().toLowerCase())) {
                    allowedWorlds.add(world.getName());
                }
            }
            allowedWorlds.removeIf(w -> !w.toLowerCase().startsWith(input));
            return allowedWorlds;
        }

        return Collections.emptyList();
    }
}
