package net.lunark.io.homes;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class HomeManager {
    private final HomeStorage storage;
    private final int maxHomes;

    public HomeManager(HomeStorage storage, int maxHomes) {
        this.storage = storage;
        this.maxHomes = maxHomes;
    }

    public CompletableFuture<Optional<Home>> getHome(UUID playerId, int slot) {
        return storage.getHome(playerId, slot);
    }

    public CompletableFuture<Boolean> setHome(UUID playerId, int slot, Home home) {
        if (slot < 1 || slot > maxHomes) {
            return CompletableFuture.completedFuture(false);
        }
        return storage.setHome(playerId, slot, home).thenApply(v -> true);
    }

    public CompletableFuture<Boolean> removeHome(UUID playerId, int slot) {
        return storage.removeHome(playerId, slot).thenApply(v -> true);
    }

    public CompletableFuture<Map<Integer, Home>> getAllHomes(UUID playerId) {
        return storage.getAllHomes(playerId);
    }

    public CompletableFuture<Integer> countHomes(UUID playerId) {
        return storage.getAllHomes(playerId).thenApply(Map::size);
    }
}