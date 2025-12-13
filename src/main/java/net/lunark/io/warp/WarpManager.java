package net.lunark.io.warp;

import net.lunark.io.commands.config.WarpConfig;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class WarpManager {
    private final WarpStorage storage;
    private final WarpConfig config;
    private final PlayerLanguageManager langManager;

    public WarpManager(WarpStorage storage, WarpConfig config, PlayerLanguageManager langManager) {
        this.storage = storage;
        this.config = config;
        this.langManager = langManager;
    }

    public CompletableFuture<Void> addWarp(String name, Location loc, UUID creator) {
        return storage.addWarp(name, loc, creator);
    }

    public CompletableFuture<Void> removeWarp(String name) {
        return storage.removeWarp(name);
    }

    public CompletableFuture<Optional<Optional<Location>>> getWarp(String name) {
        return storage.getWarp(name);
    }

    public CompletableFuture<Map<String, Location>> getAllWarps() {
        return storage.getAllWarps();
    }

    public CompletableFuture<Optional<UUID>> getCreator(String name) {
        return storage.getCreator(name);
    }

    public CompletableFuture<Boolean> exists(String name) {
        return storage.exists(name);
    }

    public CompletableFuture<Integer> getWarpCountByCreator(UUID creator) {
        return storage.getWarpCountByCreator(creator).thenApply(Long::intValue);
    }

    public int getMaxWarpsForPlayer(Player player) {
        return config.getMaxWarpsForPlayer(player);
    }

    public Duration getCooldown() {
        return config.cooldown;
    }
}