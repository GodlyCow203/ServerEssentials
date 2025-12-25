package net.godlycow.org.commands;

import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


public abstract class CommandModule {
    protected final CommandDataStorage storage;
    protected final PlayerLanguageManager langManager;

    public CommandModule(CommandDataStorage storage, PlayerLanguageManager langManager) {
        this.storage = storage;
        this.langManager = langManager;
    }

    protected abstract String getCommandName();

    protected CompletableFuture<Boolean> getBooleanState(Player player, String key, boolean defaultValue) {
        return getBooleanState(player.getUniqueId(), key, defaultValue);
    }

    protected CompletableFuture<Boolean> getBooleanState(UUID playerId, String key, boolean defaultValue) {
        return storage.getState(playerId, getCommandName(), key)
                .thenApply(opt -> opt.map(Boolean::parseBoolean).orElse(defaultValue));
    }

    protected CompletableFuture<Void> setBooleanState(Player player, String key, boolean value) {
        return setBooleanState(player.getUniqueId(), key, value);
    }

    protected CompletableFuture<Void> setBooleanState(UUID playerId, String key, boolean value) {
        return storage.setState(playerId, getCommandName(), key, String.valueOf(value));
    }

    protected CompletableFuture<Integer> getIntState(Player player, String key, int defaultValue) {
        return storage.getState(player.getUniqueId(), getCommandName(), key)
                .thenApply(opt -> opt.map(Integer::parseInt).orElse(defaultValue));
    }

    protected CompletableFuture<Void> setIntState(Player player, String key, int value) {
        return storage.setState(player.getUniqueId(), getCommandName(), key, String.valueOf(value));
    }

    protected CompletableFuture<Optional<String>> getStringState(Player player, String key) {
        return storage.getState(player.getUniqueId(), getCommandName(), key);
    }

    protected CompletableFuture<Void> setStringState(Player player, String key, String value) {
        return storage.setState(player.getUniqueId(), getCommandName(), key, value);
    }

    protected CompletableFuture<Void> clearAllStates(Player player) {
        return storage.deleteAllForCommand(player.getUniqueId(), getCommandName());
    }
}