package net.lunark.io.economy;

import net.lunark.io.database.DatabaseManager;
import net.lunark.io.commands.CommandDataStorage;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Manages persistent shop state using CommandDataStorage pattern
 * Stores: current section, page number per player
 */
public class ShopStorage {
    private final CommandDataStorage storage;
    private static final String COMMAND_KEY = "shop";

    public ShopStorage(DatabaseManager dbManager) {
        this.storage = new CommandDataStorage(null, dbManager);
    }

    public CompletableFuture<Void> setPlayerSection(UUID playerId, String sectionFile) {
        return storage.setState(playerId, COMMAND_KEY, "section", sectionFile);
    }

    public CompletableFuture<Optional<String>> getPlayerSection(UUID playerId) {
        return storage.getState(playerId, COMMAND_KEY, "section");
    }

    public CompletableFuture<Void> setPlayerPage(UUID playerId, int page) {
        return storage.setState(playerId, COMMAND_KEY, "page", String.valueOf(page));
    }

    public CompletableFuture<Integer> getPlayerPage(UUID playerId) {
        return storage.getState(playerId, COMMAND_KEY, "page")
                .thenApply(opt -> opt.map(Integer::parseInt).orElse(1));
    }

    public CompletableFuture<Void> clearPlayerState(UUID playerId) {
        return storage.setState(playerId, COMMAND_KEY, "section", "")
                .thenCompose(v -> storage.setState(playerId, COMMAND_KEY, "page", "1"));
    }
}