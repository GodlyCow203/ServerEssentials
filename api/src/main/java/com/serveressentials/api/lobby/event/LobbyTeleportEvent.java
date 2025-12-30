package com.serveressentials.api.lobby.event;

import com.serveressentials.api.lobby.LobbyLocation;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;


public final class LobbyTeleportEvent extends LobbyEvent {
    private final @NotNull LobbyLocation lobbyLocation;
    private final @Nullable Location fromLocation;
    private final @Nullable String worldKey;
    private final boolean isWorldSpecific;

    public LobbyTeleportEvent(@NotNull Player player, @NotNull LobbyLocation lobbyLocation,
                              @Nullable Location fromLocation, @Nullable String worldKey,
                              boolean isWorldSpecific) {
        super(player);
        this.lobbyLocation = Objects.requireNonNull(lobbyLocation, "lobbyLocation cannot be null");
        this.fromLocation = fromLocation;
        this.worldKey = worldKey;
        this.isWorldSpecific = isWorldSpecific;
    }

    /**
     * Gets the lobby location being teleported to.
     *
     * @return The lobby location
     */
    public @NotNull LobbyLocation getLobbyLocation() {
        return lobbyLocation;
    }

    /**
     * Gets the player's original location before teleport.
     *
     * @return The original location, or null if not available
     */
    public @Nullable Location getFromLocation() {
        return fromLocation;
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