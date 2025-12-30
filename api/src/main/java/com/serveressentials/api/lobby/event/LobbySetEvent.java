package com.serveressentials.api.lobby.event;

import com.serveressentials.api.lobby.LobbyLocation;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;


public final class LobbySetEvent extends LobbyEvent {
    private final @NotNull LobbyLocation lobbyLocation;
    private final @Nullable String worldKey;
    private final boolean isWorldSpecific;

    public LobbySetEvent(@NotNull Player player, @NotNull LobbyLocation lobbyLocation,
                         @Nullable String worldKey, boolean isWorldSpecific) {
        super(player);
        this.lobbyLocation = Objects.requireNonNull(lobbyLocation, "lobbyLocation cannot be null");
        this.worldKey = worldKey;
        this.isWorldSpecific = isWorldSpecific;
    }

    /**
     * Gets the lobby location that was set.
     *
     * @return The lobby location
     */
    public @NotNull LobbyLocation getLobbyLocation() {
        return lobbyLocation;
    }

    /**
     * Gets the world key for per-world lobby, or null for global.
     *
     * @return The world key
     */
    public @Nullable String getWorldKey() {
        return worldKey;
    }

    /**
     * Checks if this is a world-specific lobby.
     *
     * @return true if world-specific, false if global
     */
    public boolean isWorldSpecific() {
        return isWorldSpecific;
    }
}