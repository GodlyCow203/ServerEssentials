package com.serveressentials.api.home;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface HomeStorage {

    CompletableFuture<Void> saveHome(UUID playerId, int slot, Home home);

    CompletableFuture<Optional<Home>> loadHome(UUID playerId, int slot);

    CompletableFuture<Void> deleteHome(UUID playerId, int slot);

    CompletableFuture<Map<Integer, Home>> loadAllHomes(UUID playerId);
}