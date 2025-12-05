package net.lunark.io.back;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


public class BackManager {
    private final Map<UUID, Location> backLocations = new HashMap<>();
    private final BackDataStorage storage;

    public BackManager(BackDataStorage storage) {
        this.storage = storage;
    }


    public CompletableFuture<Void> setLastLocation(UUID uuid, Location location) {
        if (location == null || location.getWorld() == null) {
            return CompletableFuture.completedFuture(null);
        }

        Location cloned = location.clone();
        backLocations.put(uuid, cloned);

        return storage.saveBackLocation(uuid, cloned);
    }


    public CompletableFuture<Location> getLastLocation(UUID uuid) {
        Location cached = backLocations.get(uuid);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached.clone());
        }

        return storage.loadBackLocation(uuid).thenApply(opt -> {
            if (opt.isPresent()) {
                Location loaded = opt.get();
                backLocations.put(uuid, loaded);
                return loaded.clone();
            }
            return null;
        });
    }


    public CompletableFuture<Boolean> hasBack(UUID uuid) {
        if (backLocations.containsKey(uuid)) {
            return CompletableFuture.completedFuture(true);
        }

        return storage.hasBackLocation(uuid);
    }


    public CompletableFuture<Void> clearBack(UUID uuid) {
        backLocations.remove(uuid);
        return storage.deleteBackLocation(uuid);
    }


    public void loadAllBackLocations() {

    }
}