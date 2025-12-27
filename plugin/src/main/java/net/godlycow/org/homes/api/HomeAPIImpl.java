package net.godlycow.org.homes.api;


import com.serveressentials.api.home.Home;
import com.serveressentials.api.home.HomeAPI;
import net.godlycow.org.homes.HomeManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class HomeAPIImpl implements HomeAPI {

    private final HomeManager manager;

    public HomeAPIImpl(HomeManager manager) {
        this.manager = manager;
    }

    @Override
    public CompletableFuture<Optional<Home>> getHome(UUID playerId, int slot) {
        return manager.getHome(playerId, slot)
                .thenApply(opt -> opt.map(h -> (Home) h));
    }

    @Override
    public CompletableFuture<Boolean> setHome(UUID playerId, int slot, Home home) {

        return manager.setHome(playerId, slot, (net.godlycow.org.homes.model.Home) home);
    }

    @Override
    public CompletableFuture<Boolean> removeHome(UUID playerId, int slot) {
        return manager.removeHome(playerId, slot);
    }

    @Override
    public CompletableFuture<Map<Integer, Home>> getAllHomes(UUID playerId) {
        return manager.getAllHomes(playerId)
                .thenApply(map -> (Map<Integer, Home>) (Map<?, ?>) map);
    }

    @Override
    public CompletableFuture<Integer> countHomes(UUID playerId) {
        return manager.countHomes(playerId);
    }

    @Override
    public boolean canSetHome(UUID playerId) {
        return manager.canSetHome(playerId);
    }

    @Override
    public long getRemainingSetCooldown(UUID playerId) {
        return manager.getRemainingSetCooldown(playerId);
    }

    @Override
    public boolean canTeleport(UUID playerId) {
        return manager.canTeleport(playerId);
    }

    @Override
    public long getRemainingTeleportCooldown(UUID playerId) {
        return manager.getRemainingTeleportCooldown(playerId);
    }

    @Override
    public void clearCooldowns(UUID playerId) {
        manager.clearCooldowns(playerId);
    }

    @Override
    public void clearCache() {
        manager.clearCache();
    }

    @Override
    public void reload() {
        manager.reload();
    }
}