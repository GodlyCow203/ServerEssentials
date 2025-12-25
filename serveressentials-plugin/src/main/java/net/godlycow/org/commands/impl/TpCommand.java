package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.config.TpConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class TpCommand implements TabExecutor {

    private static final String PERMISSION = "serveressentials.command.tp";

    private final PlayerLanguageManager langManager;
    private final TpConfig config;

    public TpCommand(PlayerLanguageManager langManager, TpConfig config) {
        this.langManager = langManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.tp.only-player",
                    "<red>Only players can use this command!"));
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.tp.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(langManager.getMessageFor(player, "commands.tp.usage",
                    "<red>Usage: /tp <player|x y z>"));
            return true;
        }

        if (args.length == 1) {

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(langManager.getMessageFor(player, "commands.tp.player-not-found",
                        "<red>Player not found: <yellow>{player}</yellow>",
                        ComponentPlaceholder.of("{player}", args[0])));
                return true;
            }

            player.teleport(target);
            player.sendMessage(langManager.getMessageFor(player, "commands.tp.success",
                    "<green>Teleported to <white>{player}</white>.",
                    ComponentPlaceholder.of("{player}", target.getName())));
            return true;
        }


        if (args.length == 2) {

            Player p1 = Bukkit.getPlayer(args[0]);
            Player p2 = Bukkit.getPlayer(args[1]);

            if (p1 == null || p2 == null) {
                player.sendMessage(langManager.getMessageFor(player, "commands.tp.player-not-found",
                        "<red>Player not found."));
                return true;
            }

            p1.teleport(p2);
            player.sendMessage(langManager.getMessageFor(player, "commands.tp.success-other",
                    "<green>Teleported <white>{p1}</white> to <white>{p2}</white>.",
                    ComponentPlaceholder.of("{p1}", p1.getName()),
                    ComponentPlaceholder.of("{p2}", p2.getName())));
            return true;
        }

        int index = 0;
        Player targetPlayer = player;

        if (args.length == 3 || args.length == 5) {
        } else if (args.length == 4 || args.length == 6) {
            Player specified = Bukkit.getPlayer(args[0]);
            if (specified == null) {
                player.sendMessage(langManager.getMessageFor(player, "commands.tp.player-not-found",
                        "<red>Player not found: <yellow>{player}</yellow>",
                        ComponentPlaceholder.of("{player}", args[0])));
                return true;
            }
            targetPlayer = specified;
            index = 1;
        } else {
            player.sendMessage(langManager.getMessageFor(player, "commands.tp.usage",
                    "<red>Usage: /tp <player> <player|coords>"));
            return true;
        }

        Location origin = targetPlayer.getLocation();
        World world = origin.getWorld();

        double x = parseCoord(origin.getX(), args[index]);
        double y = parseCoord(origin.getY(), args[index + 1]);
        double z = parseCoord(origin.getZ(), args[index + 2]);

        float yaw = origin.getYaw();
        float pitch = origin.getPitch();

        if (args.length - index >= 5) {
            yaw = parseRotation(origin.getYaw(), args[index + 3]);
            pitch = parseRotation(origin.getPitch(), args[index + 4]);
        }

        Location newLoc = new Location(world, x, y, z, yaw, pitch);
        targetPlayer.teleport(newLoc);

        if (targetPlayer.equals(player)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.tp.success-coords",
                    "<green>Teleported to <white>{x} {y} {z}</white>.",
                    ComponentPlaceholder.of("{x}", x + ""),
                    ComponentPlaceholder.of("{y}", y + ""),
                    ComponentPlaceholder.of("{z}", z + "")));
        } else {
            player.sendMessage(langManager.getMessageFor(player, "commands.tp.success-other",
                    "<green>Teleported <white>{p1}</white> to coordinates.",
                    ComponentPlaceholder.of("{p1}", targetPlayer.getName())));
        }

        return true;
    }


    private double parseCoord(double base, String input) {
        if (input.startsWith("~")) {
            if (input.equals("~")) return base;
            return base + Double.parseDouble(input.substring(1));
        }
        if (input.startsWith("^")) {
            return base;
        }
        return Double.parseDouble(input);
    }

    private float parseRotation(float base, String input) {
        if (input.startsWith("~")) {
            if (input.equals("~")) return base;
            return base + Float.parseFloat(input.substring(1));
        }
        return Float.parseFloat(input);
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return Collections.emptyList();

        List<String> list = new ArrayList<>();

        if (args.length == 1 || args.length == 2) {
            // Suggest player names
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
                    list.add(p.getName());
                }
            }
            return list;
        }

        // Coordinates suggestion
        if (args.length >= 3 && args.length <= 6) {
            list.add("~");
            list.add("~ ~");
            list.add("~ ~ ~");
            return list;
        }

        return Collections.emptyList();
    }
}
