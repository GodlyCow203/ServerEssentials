package net.lunark.io.commands.impl;

import net.lunark.io.commands.CommandDataStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class MagnetStorage {
    private final CommandDataStorage commandDataStorage;
    private final Set<UUID> activeMagnets = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Plugin plugin;

    public MagnetStorage(Plugin plugin, CommandDataStorage commandDataStorage) {
        this.plugin = plugin;
        this.commandDataStorage = commandDataStorage;
        // Load currently online players individually
        for (Player player : Bukkit.getOnlinePlayers()) {
            loadPlayerState(player);
        }
    }

    private void loadPlayerState(Player player) {
        commandDataStorage.getState(player.getUniqueId(), "magnet", "enabled").thenAccept(opt -> {
            if (opt.isPresent() && Boolean.parseBoolean(opt.get())) {
                activeMagnets.add(player.getUniqueId());
            }
        }).exceptionally(ex -> {
            plugin.getLogger().warning("Failed to load magnet state for " + player.getName() + ": " + ex.getMessage());
            return null;
        });
    }

    public void onPlayerJoin(Player player) {
        loadPlayerState(player);
    }

    public void onPlayerQuit(Player player) {
        activeMagnets.remove(player.getUniqueId());
    }

    public CompletableFuture<Void> enableMagnet(Player player) {
        activeMagnets.add(player.getUniqueId());
        return commandDataStorage.setState(player.getUniqueId(), "magnet", "enabled", "true");
    }

    public CompletableFuture<Void> disableMagnet(Player player) {
        activeMagnets.remove(player.getUniqueId());
        return commandDataStorage.deleteState(player.getUniqueId(), "magnet", "enabled");
    }

    public boolean isEnabled(Player player) {
        return activeMagnets.contains(player.getUniqueId());
    }

    public Set<Player> getActivePlayers() {
        Set<Player> players = new HashSet<>();
        for (UUID id : activeMagnets) {
            Player player = Bukkit.getPlayer(id);
            if (player != null && player.isOnline()) {
                players.add(player);
            }
        }
        return players;
    }
}