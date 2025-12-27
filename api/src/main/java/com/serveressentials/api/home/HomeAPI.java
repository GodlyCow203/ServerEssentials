package com.serveressentials.api.home;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface HomeAPI {

    CompletableFuture<Optional<Home>> getHome(UUID playerId, int slot);

    CompletableFuture<Boolean> setHome(UUID playerId, int slot, Home home);

    CompletableFuture<Boolean> removeHome(UUID playerId, int slot);

    CompletableFuture<Map<Integer, Home>> getAllHomes(UUID playerId);

    CompletableFuture<Integer> countHomes(UUID playerId);

    boolean canSetHome(UUID playerId);

    long getRemainingSetCooldown(UUID playerId);

    boolean canTeleport(UUID playerId);

    long getRemainingTeleportCooldown(UUID playerId);

    void clearCooldowns(UUID playerId);

    void clearCache();

    void reload();
}