package net.godlycow.org.homes.api;

import com.serveressentials.api.home.Home;
import com.serveressentials.api.home.HomeAPI;
import net.godlycow.org.homes.HomeManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.HashMap;

public class HomeAPIImpl implements HomeAPI {
    private final HomeManager homeManager;

    public HomeAPIImpl(HomeManager homeManager) {
        this.homeManager = homeManager;
    }

    @Override
    public CompletableFuture<Boolean> setHome(Player player, int slot, String name, Location location) {
        net.godlycow.org.homes.model.Home internalHome = new net.godlycow.org.homes.model.Home(name, location);
        return homeManager.setHome(player.getUniqueId(), slot, internalHome);
    }

    @Override
    public CompletableFuture<Optional<Home>> getHome(Player player, int slot) {
        return homeManager.getHome(player.getUniqueId(), slot)
                .thenApply(opt -> {
                    if (opt.isPresent()) {
                        net.godlycow.org.homes.model.Home internal = opt.get();
                        return Optional.of(new Home(internal.getName(), internal.toLocation()));
                    }
                    return Optional.empty();
                });
    }

    @Override
    public CompletableFuture<Map<Integer, Home>> getAllHomes(Player player) {
        return homeManager.getAllHomes(player.getUniqueId())
                .thenApply(map -> {
                    Map<Integer, Home> result = new HashMap<>();
                    for (Map.Entry<Integer, net.godlycow.org.homes.model.Home> entry : map.entrySet()) {
                        net.godlycow.org.homes.model.Home h = entry.getValue();
                        if (h != null && h.toLocation() != null) {
                            result.put(entry.getKey(), new Home(h.getName(), h.toLocation()));
                        }
                    }
                    return result;
                });
    }

    @Override
    public CompletableFuture<Boolean> removeHome(Player player, int slot) {
        return homeManager.removeHome(player.getUniqueId(), slot);
    }

    @Override
    public int getMaxHomes(Player player) {
        return homeManager.config.maxHomes;

        // OR if that doesn't work, use a simple approach:
        // return 10; // Default value
    }

    @Override
    public CompletableFuture<Integer> getHomeCount(Player player) {
        return homeManager.countHomes(player.getUniqueId());
    }
}