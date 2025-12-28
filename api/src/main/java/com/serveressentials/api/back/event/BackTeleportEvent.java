package com.serveressentials.api.back.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class BackTeleportEvent extends BackEvent {
    private final @NotNull Location from;
    private final @NotNull Location to;

    public BackTeleportEvent(@NotNull Player player, @NotNull BackType backType,
                             @NotNull Location from, @NotNull Location to) {
        super(player, backType);
        this.from = Objects.requireNonNull(from, "from location cannot be null").clone();
        this.to = Objects.requireNonNull(to, "to location cannot be null").clone();
    }

    public @NotNull Location getFrom() { return from.clone(); }
    public @NotNull Location getTo() { return to.clone(); }
}
