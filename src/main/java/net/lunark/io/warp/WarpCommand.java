package net.lunark.io.warp;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.lunark.io.ServerEssentials;
import net.lunark.io.util.WarpMessages;

import java.util.UUID;

public class WarpCommand implements CommandExecutor {

    private final ServerEssentials plugin;
    private final WarpManager warpManager;
    private final WarpMessages messages;

    public WarpCommand(ServerEssentials plugin) {
        this.plugin = plugin;
        this.warpManager = plugin.getWarpManager();
        this.messages = plugin.getWarpMessages();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command!"));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(messages.get("usage", "<command>", "/warp <name>"));
            return true;
        }

        String warpName = args[0].toLowerCase();

        switch (command.getName().toLowerCase()) {
            case "warp" -> {
                if (!player.hasPermission("serveressentials.warp")) {
                    player.sendMessage(messages.get("no-permission"));
                    return true;
                }

                if (!warpManager.exists(warpName)) {
                    player.sendMessage(messages.get("warp-not-found", "<warp>", warpName));
                    return true;
                }

                Location loc = warpManager.getWarp(warpName);
                player.teleport(loc);
                player.sendMessage(messages.get("warp-success", "<warp>", warpName));
            }

            case "setwarp" -> {
                if (!player.hasPermission("serveressentials.setwarp")) {
                    player.sendMessage(messages.get("no-permission"));
                    return true;
                }

                // Optional fine-grained permission check
                int maxWarps = Integer.MAX_VALUE;
                for (int i = 1; i <= 100; i++) {
                    if (player.hasPermission("serveressentials.set.warp" + i)) {
                        maxWarps = i;
                    }
                }

                long playerWarps = warpManager.getWarps().entrySet().stream()
                        .filter(e -> player.getUniqueId().equals(warpManager.getCreator(e.getKey())))
                        .count();

                if (playerWarps >= maxWarps) {
                    player.sendMessage(messages.get("warp-limit-reached", "<limit>", String.valueOf(maxWarps)));
                    return true;
                }

                warpManager.addWarp(warpName, player.getLocation(), player.getUniqueId());
                player.sendMessage(messages.get("warp-set", "<warp>", warpName));
            }

            case "delwarp" -> {
                if (!player.hasPermission("serveressentials.delwarp")) {
                    player.sendMessage(messages.get("no-permission"));
                    return true;
                }

                if (!warpManager.exists(warpName)) {
                    player.sendMessage(messages.get("warp-not-found", "<warp>", warpName));
                    return true;
                }

                UUID creator = warpManager.getCreator(warpName);
                if (!player.getUniqueId().equals(creator) && !player.hasPermission("serveressentials.delwarp.all")) {
                    player.sendMessage(messages.get("warp-no-permission-delete"));
                    return true;
                }

                warpManager.removeWarp(warpName);
                player.sendMessage(messages.get("warp-removed", "<warp>", warpName));
            }

        }

        return true;
    }
}
