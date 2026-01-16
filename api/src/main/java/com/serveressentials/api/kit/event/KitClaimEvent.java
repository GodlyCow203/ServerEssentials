package com.serveressentials.api.kit.event;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;


public final class KitClaimEvent extends KitEvent {
    private final @NotNull String kitId;
    private final @NotNull String kitName;

    public KitClaimEvent(@NotNull Player player, @NotNull String kitId, @NotNull String kitName) {
        super(player);
        this.kitId = Objects.requireNonNull(kitId, "kitId cannot be null");
        this.kitName = Objects.requireNonNull(kitName, "kitName cannot be null");
    }

    /**
     * Gets the ID of the kit being claimed.
     *
     * @return The kit ID
     */
    public @NotNull String getKitId() {
        return kitId;
    }

    /**
     * Gets the display name of the kit being claimed.
     *
     * @return The kit name
     */
    public @NotNull String getKitName() {
        return kitName;
    }
}