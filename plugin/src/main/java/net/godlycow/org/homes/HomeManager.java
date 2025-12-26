package net.godlycow.org.homes;

import net.godlycow.org.homes.model.Home;
import net.godlycow.org.homes.storage.HomeStorage;
import net.godlycow.org.commands.config.HomesConfig;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class HomeManager {
    private final HomeStorage storage;
    private final HomesConfig config;
    private final Map<UUID, Long> lastSetTimes = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastTeleportTimes = new ConcurrentHashMap<>();
    private final Map<UUID, Map<Integer, Home>> homeCache = new ConcurrentHashMap<>();

    public HomeManager(HomeStorage storage, HomesConfig config) {
        this.storage = storage;
        this.config = config;
    }

    public CompletableFuture<Optional<Home>> getHome(UUID playerId, int slot) {
        return storage.getHome(playerId, slot)
                .exceptionally(ex -> {
                    Bukkit.getLogger().log(Level.WARNING, "Failed to fetch home for " + playerId + " slot " + slot, ex);
                    return Optional.empty();
                });
    }

    public CompletableFuture<Boolean> setHome(UUID playerId, int slot, Home home) {
        if (slot < 1 || slot > config.maxHomes) {
            return CompletableFuture.completedFuture(false);
        }

        return storage.setHome(playerId, slot, home)
                .thenApply(v -> {
                    lastSetTimes.put(playerId, System.currentTimeMillis());
                    homeCache.computeIfAbsent(playerId, k -> new HashMap<>()).put(slot, home);
                    return true;
                })
                .exceptionally(ex -> {
                    Bukkit.getLogger().log(Level.WARNING, "Failed to set home for " + playerId, ex);
                    return false;
                });
    }

    public CompletableFuture<Boolean> removeHome(UUID playerId, int slot) {
        return storage.removeHome(playerId, slot)
                .thenApply(v -> {
                    if (homeCache.containsKey(playerId)) {
                        homeCache.get(playerId).remove(slot);
                    }
                    return true;
                })
                .exceptionally(ex -> {
                    Bukkit.getLogger().log(Level.WARNING, "Failed to remove home for " + playerId, ex);
                    return false;
                });
    }

    public CompletableFuture<Map<Integer, Home>> getAllHomes(UUID playerId) {
        return storage.getAllHomes(playerId)
                .thenApply(homes -> {
                    Map<Integer, Home> validHomes = new HashMap<>();
                    for (Map.Entry<Integer, Home> entry : homes.entrySet()) {
                        if (entry.getValue() != null && entry.getValue().toLocation() != null) {
                            validHomes.put(entry.getKey(), entry.getValue());
                        }
                    }
                    homeCache.put(playerId, validHomes);
                    return validHomes;
                })
                .exceptionally(ex -> {
                    Bukkit.getLogger().log(Level.WARNING, "Failed to fetch all homes for " + playerId, ex);
                    return new HashMap<>();
                });
    }

    public CompletableFuture<Integer> countHomes(UUID playerId) {
        return getAllHomes(playerId).thenApply(Map::size);
    }

    public boolean canSetHome(UUID playerId) {
        if (config.setCooldownSeconds <= 0) return true;
        Long lastSet = lastSetTimes.get(playerId);
        if (lastSet == null) return true;
        long elapsed = (System.currentTimeMillis() - lastSet) / 1000;
        return elapsed >= config.setCooldownSeconds;
    }

    public long getRemainingSetCooldown(UUID playerId) {
        if (config.setCooldownSeconds <= 0) return 0;
        Long lastSet = lastSetTimes.get(playerId);
        if (lastSet == null) return 0;
        long elapsed = (System.currentTimeMillis() - lastSet) / 1000;
        return Math.max(0, config.setCooldownSeconds - elapsed);
    }

    public boolean canTeleport(UUID playerId) {
        if (config.teleportCooldownSeconds <= 0) return true;
        Long lastTeleport = lastTeleportTimes.get(playerId);
        if (lastTeleport == null) return true;
        long elapsed = (System.currentTimeMillis() - lastTeleport) / 1000;
        return elapsed >= config.teleportCooldownSeconds;
    }

    public long getRemainingTeleportCooldown(UUID playerId) {
        if (config.teleportCooldownSeconds <= 0) return 0;
        Long lastTeleport = lastTeleportTimes.get(playerId);
        if (lastTeleport == null) return 0;
        long elapsed = (System.currentTimeMillis() - lastTeleport) / 1000;
        return Math.max(0, config.teleportCooldownSeconds - elapsed);
    }

    public void updateLastTeleportTime(UUID playerId) {
        lastTeleportTimes.put(playerId, System.currentTimeMillis());
    }

    public void reload() {
        config.reload();
        homeCache.clear();
        Bukkit.getLogger().info("[ServerEssentials] Home system reloaded");
    }


    public void clearCooldowns(UUID playerId) {
        lastSetTimes.remove(playerId);
        lastTeleportTimes.remove(playerId);
    }

    public void clearCache() {
        homeCache.clear();
    }
}