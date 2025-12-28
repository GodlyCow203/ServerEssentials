package com.godlycow.testapi;

import com.serveressentials.api.back.BackAPI;
import com.serveressentials.api.back.event.BackEvent;
import com.serveressentials.api.back.event.BackLocationSaveEvent;
import com.serveressentials.api.back.event.BackTeleportEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class BackAPITestCommand implements CommandExecutor, Listener {
    private final JavaPlugin plugin;
    private BackAPI backAPI;

    public BackAPITestCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void setBackAPI(BackAPI backAPI) {
        this.backAPI = backAPI;
        plugin.getLogger().info("BackAPI test command received API instance: " + (backAPI != null));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can test the Back API");
            return true;
        }

        if (backAPI == null) {
            player.sendMessage("§cBackAPI not available yet. Please wait for plugin to fully load.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§e--- Back API Test Commands ---");
            player.sendMessage("§7/backapitest teleport §f- Teleport to your back location");
            player.sendMessage("§7/backapitest lobby §f- Teleport to lobby");
            player.sendMessage("§7/backapitest death §f- Teleport to death location");
            player.sendMessage("§7/backapitest save §f- Save current location as back");
            player.sendMessage("§7/backapitest check §f- Check if you have a back location");
            player.sendMessage("§7/backapitest clear §f- Clear your back location");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "teleport" -> testTeleport(player);
            case "lobby" -> testLobby(player);
            case "death" -> testDeath(player);
            case "save" -> testSave(player);
            case "check" -> testCheck(player);
            case "clear" -> testClear(player);
            default -> player.sendMessage("§cUnknown test. Use /backapitest for help");
        }
        return true;
    }

    private void testTeleport(Player player) {
        player.sendMessage("§eTesting teleportBack()...");
        backAPI.teleportBack(player).thenAccept(success -> {
            if (success) {
                player.sendMessage("§a✓ Teleport successful!");
            } else {
                player.sendMessage("§c✗ Teleport failed!");
            }
        });
    }

    private void testLobby(Player player) {
        player.sendMessage("§eTesting teleportToLobby()...");
        backAPI.teleportToLobby(player).thenAccept(success -> {
            if (success) {
                player.sendMessage("§a✓ Lobby teleport successful!");
            } else {
                player.sendMessage("§c✗ Lobby teleport failed!");
            }
        });
    }

    private void testDeath(Player player) {
        player.sendMessage("§eTesting teleportToDeath()...");
        backAPI.teleportToDeath(player).thenAccept(success -> {
            if (success) {
                player.sendMessage("§a✓ Death location teleport successful!");
            } else {
                player.sendMessage("§c✗ Death location teleport failed!");
            }
        });
    }

    private void testSave(Player player) {
        player.sendMessage("§eTesting setBackLocation()...");
        Location loc = player.getLocation();
        backAPI.setBackLocation(player, loc).thenRun(() -> {
            player.sendMessage("§a✓ Location saved: " + formatLocation(loc));
        });
    }

    private void testCheck(Player player) {
        player.sendMessage("§eTesting hasBackLocation()...");
        backAPI.hasBackLocation(player).thenAccept(has -> {
            player.sendMessage("§a✓ Has back location: " + has);
        });
    }

    private void testClear(Player player) {
        player.sendMessage("§eTesting clearBackLocation()...");
        backAPI.clearBackLocation(player).thenRun(() -> {
            player.sendMessage("§a✓ Back location cleared!");
        });
    }

    private String formatLocation(Location loc) {
        return String.format("%.1f, %.1f, %.1f in %s", loc.getX(), loc.getY(), loc.getZ(), loc.getWorld().getName());
    }

    @EventHandler
    public void onBackTeleport(BackTeleportEvent event) {
        plugin.getLogger().info(String.format("BackTeleportEvent: %s teleported via %s from %s to %s",
                event.getPlayer().getName(),
                event.getBackType(),
                formatLocation(event.getFrom()),
                formatLocation(event.getTo())));
    }

    @EventHandler
    public void onBackLocationSave(BackLocationSaveEvent event) {
        plugin.getLogger().info(String.format("BackLocationSaveEvent: %s saved location %s",
                event.getPlayer().getName(),
                formatLocation(event.getSavedLocation())));
    }
}