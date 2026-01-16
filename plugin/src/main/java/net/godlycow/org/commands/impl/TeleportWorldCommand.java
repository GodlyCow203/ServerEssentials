package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.config.TeleportWorldConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class TeleportWorldCommand implements CommandExecutor, TabCompleter {
    private static final String PERMISSION_NODE = "essc.command.tpp";
    private static final String PERMISSION_OTHERS = "essc.command.tpp.others";
    private static final String PERMISSION_WORLD_PREFIX = "essc.command.tpp.world.";

    private final PlayerLanguageManager langManager;
    private final TeleportWorldConfig config;

    public TeleportWorldCommand(PlayerLanguageManager langManager, TeleportWorldConfig config) {
        this.langManager = langManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.tpp.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION_NODE)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.tpp.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION_NODE)));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(langManager.getMessageFor(player, "commands.tpp.usage",
                    "<red>Usage: /tpp <player> <world> [x y z]"));
            return true;
        }

        String targetName = args[0];
        String worldName = args[1];
        String worldPermission = PERMISSION_WORLD_PREFIX + worldName.toLowerCase();

        if (!player.hasPermission(worldPermission)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.tpp.no-permission-world",
                    "<red>You don't have permission to access world <yellow>{world}</yellow>!",
                    ComponentPlaceholder.of("{permission}", worldPermission),
                    ComponentPlaceholder.of("{world}", worldName)));
            return true;
        }

        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null || !target.isOnline()) {
            player.sendMessage(langManager.getMessageFor(player, "commands.tpp.player-not-found",
                    "<red>Player <yellow>{player}</yellow> not found!",
                    ComponentPlaceholder.of("{player}", targetName)));
            return true;
        }

        boolean isSelf = target.equals(player);
        if (!isSelf && !player.hasPermission(PERMISSION_OTHERS)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.tpp.no-permission-others",
                    "<red>You need permission <yellow>{permission}</yellow> to teleport other players!",
                    ComponentPlaceholder.of("{permission}", PERMISSION_OTHERS)));
            return true;
        }

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            player.sendMessage(langManager.getMessageFor(player, "commands.tpp.world-not-loaded",
                    "<red>World <yellow>{world}</yellow> is not loaded!",
                    ComponentPlaceholder.of("{world}", worldName)));
            return true;
        }

        Location loc;
        if (args.length >= 5) {
            try {
                double x = Double.parseDouble(args[2]);
                double y = Double.parseDouble(args[3]);
                double z = Double.parseDouble(args[4]);
                loc = new Location(world, x, y, z);
            } catch (NumberFormatException e) {
                player.sendMessage(langManager.getMessageFor(player, "commands.tpp.invalid-coords",
                        "<red>Invalid coordinates! Must be numbers."));
                return true;
            }
        } else {
            loc = world.getSpawnLocation();
        }

        target.teleport(loc);

        if (!isSelf) {
            player.sendMessage(langManager.getMessageFor(player, "commands.tpp.teleported-target",
                    "<green>Teleported <yellow>{target}</yellow> to world <yellow>{world}</yellow>!",
                    ComponentPlaceholder.of("{target}", target.getName()),
                    ComponentPlaceholder.of("{world}", worldName)));
        }

        target.sendMessage(langManager.getMessageFor(target, "commands.tpp.teleported-self",
                "<green>You were teleported to world <yellow>{world}</yellow> at <gray>({x}, {y}, {z})",
                ComponentPlaceholder.of("{world}", worldName),
                ComponentPlaceholder.of("{x}", String.format("%.1f", loc.getX())),
                ComponentPlaceholder.of("{y}", String.format("%.1f", loc.getY())),
                ComponentPlaceholder.of("{z}", String.format("%.1f", loc.getZ()))));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) return Collections.emptyList();

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> suggestions = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(input)) {
                    suggestions.add(p.getName());
                }
            }
            return suggestions;
        }

        if (args.length == 2) {
            String input = args[1].toLowerCase();
            List<String> allowedWorlds = new ArrayList<>();
            for (World world : Bukkit.getWorlds()) {
                String worldPerm = PERMISSION_WORLD_PREFIX + world.getName().toLowerCase();
                if (player.hasPermission(worldPerm)) {
                    allowedWorlds.add(world.getName());
                }
            }
            allowedWorlds.removeIf(w -> !w.toLowerCase().startsWith(input));
            return allowedWorlds;
        }

        return Collections.emptyList();
    }
}