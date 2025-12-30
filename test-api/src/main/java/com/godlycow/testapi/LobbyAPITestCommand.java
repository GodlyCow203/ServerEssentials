package com.godlycow.testapi;

import com.serveressentials.api.lobby.LobbyAPI;
import com.serveressentials.api.lobby.LobbyLocation;
import com.serveressentials.api.lobby.event.LobbyTeleportEvent;
import com.serveressentials.api.lobby.event.LobbySetEvent;
import com.serveressentials.api.lobby.event.LobbyRemoveEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Test command for LobbyAPI functionality.
 */
public final class LobbyAPITestCommand implements CommandExecutor, Listener {
    private final JavaPlugin plugin;
    private LobbyAPI api;


    public LobbyAPITestCommand(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void setAPI(@NotNull LobbyAPI api) {
        this.api = api;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        if (api == null) {
            player.sendMessage("§cLobbyAPI is not yet available. Please try again in a moment.");
            return true;
        }

        // Command: /lobbyapitest or /lobbyapitest teleport - teleport to lobby
        if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("teleport"))) {
            api.teleportToLobby(player).thenAccept(success -> {
                if (!success) {
                    player.sendMessage("§cFailed to teleport to lobby! No lobby set or you don't have permission.");
                }
            });
            return true;
        }

        // Command: /lobbyapitest teleport <world> - teleport to world-specific lobby
        if (args.length == 2 && args[0].equalsIgnoreCase("teleport")) {
            String worldKey = args[1];
            api.teleportToLobby(player, worldKey).thenAccept(success -> {
                if (!success) {
                    player.sendMessage("§cFailed to teleport to lobby for world '" + worldKey + "'!");
                }
            });
            return true;
        }

        // Command: /lobbyapitest set [world] - set lobby at player's location
        if (args.length >= 1 && args[0].equalsIgnoreCase("set")) {
            String worldKey = args.length == 2 ? args[1] : null;
            api.setLobby(player, player.getLocation(), worldKey).thenAccept(v -> {
                player.sendMessage("§aLobby set" + (worldKey != null ? " for world '" + worldKey + "'" : "") + "!");
            }).exceptionally(ex -> {
                player.sendMessage("§cFailed to set lobby: " + ex.getMessage());
                return null;
            });
            return true;
        }

        // Command: /lobbyapitest remove [world] - remove lobby
        if (args.length >= 1 && args[0].equalsIgnoreCase("remove")) {
            String worldKey = args.length == 2 ? args[1] : null;
            api.removeLobby(worldKey).thenAccept(v -> {
                player.sendMessage("§aLobby removed" + (worldKey != null ? " for world '" + worldKey + "'" : "") + "!");
            }).exceptionally(ex -> {
                player.sendMessage("§cFailed to remove lobby: " + ex.getMessage());
                return null;
            });
            return true;
        }

        // Command: /lobbyapitest check [world] - check if lobby exists
        if (args.length >= 1 && args[0].equalsIgnoreCase("check")) {
            String worldKey = args.length == 2 ? args[1] : null;
            api.hasLobby(worldKey).thenAccept(has -> {
                player.sendMessage("§eLobby " + (worldKey != null ? "for world '" + worldKey + "' " : "") +
                        (has ? "§aexists" : "§cdoes not exist"));
            });
            return true;
        }

        // Command: /lobbyapitest get [world] - get lobby location
        if (args.length >= 1 && args[0].equalsIgnoreCase("get")) {
            String worldKey = args.length == 2 ? args[1] : null;
            api.getLobby(worldKey).thenAccept(opt -> {
                if (opt.isPresent()) {
                    LobbyLocation loc = opt.get();
                    player.sendMessage("§6Lobby location: " + loc.getWorld() + " " +
                            String.format("%.1f, %.1f, %.1f", loc.getX(), loc.getY(), loc.getZ()));
                } else {
                    player.sendMessage("§cNo lobby location found!");
                }
            });
            return true;
        }

        // Command: /lobbyapitest status - show API status
        if (args.length == 1 && args[0].equalsIgnoreCase("status")) {
            player.sendMessage("§6LobbyAPI Status:");
            player.sendMessage("§7Enabled: " + api.isEnabled());
            api.getLobby(null).thenAccept(opt -> {
                if (opt.isPresent()) {
                    LobbyLocation loc = opt.get();
                    player.sendMessage("§7Global lobby: " + loc.getWorld() + " " +
                            String.format("%.1f, %.1f, %.1f", loc.getX(), loc.getY(), loc.getZ()));
                } else {
                    player.sendMessage("§7Global lobby: Not set");
                }
            });
            return true;
        }

        // Command: /lobbyapitest reload - reload configuration
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            api.reload().thenAccept(v -> {
                player.sendMessage("§aLobby configuration reloaded!");
            });
            return true;
        }

        sendUsage(player);
        return true;
    }

    @EventHandler
    public void onLobbyTeleport(@NotNull LobbyTeleportEvent event) {
        plugin.getLogger().info("[LobbyAPITest] " + event.getPlayer().getName() +
                " teleported to lobby" +
                (event.isWorldSpecific() ? " for world '" + event.getWorldKey() + "'" : "") +
                " at " + event.getLobbyLocation());
    }

    @EventHandler
    public void onLobbySet(@NotNull LobbySetEvent event) {
        plugin.getLogger().info("[LobbyAPITest] " + event.getPlayer().getName() +
                " set lobby" +
                (event.isWorldSpecific() ? " for world '" + event.getWorldKey() + "'" : "") +
                " at " + event.getLobbyLocation());
    }

    @EventHandler
    public void onLobbyRemove(@NotNull LobbyRemoveEvent event) {
        plugin.getLogger().info("[LobbyAPITest] " + event.getPlayer().getName() +
                " removed lobby" +
                (event.isWorldSpecific() ? " for world '" + event.getWorldKey() + "'" : ""));
    }

    private void sendUsage(@NotNull Player player) {
        player.sendMessage("§6LobbyAPI Test Command Usage:");
        player.sendMessage("§7/lobbyapitest - Teleport to lobby");
        player.sendMessage("§7/lobbyapitest teleport [world] - Teleport to lobby");
        player.sendMessage("§7/lobbyapitest set [world] - Set lobby at your location");
        player.sendMessage("§7/lobbyapitest remove [world] - Remove lobby");
        player.sendMessage("§7/lobbyapitest check [world] - Check if lobby exists");
        player.sendMessage("§7/lobbyapitest get [world] - Get lobby location");
        player.sendMessage("§7/lobbyapitest status - Show API status");
        player.sendMessage("§7/lobbyapitest reload - Reload configuration");
    }
}