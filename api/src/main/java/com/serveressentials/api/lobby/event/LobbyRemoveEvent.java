package com.serveressentials.api.lobby.event;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public final class LobbyRemoveEvent extends LobbyEvent {
    private final @Nullable String worldKey;
    private final boolean isWorldSpecific;

    public LobbyRemoveEvent(@NotNull Player player, @Nullable String worldKey, boolean isWorldSpecific) {
        super(player);
        this.worldKey = worldKey;
        this.isWorldSpecific = isWorldSpecific;
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