package com.serveressentials.api.warp.event;

import com.serveressentials.api.warp.WarpData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;

public final class WarpTeleportEvent extends WarpEvent {
    private final @NotNull WarpData warpData;

    public WarpTeleportEvent(@NotNull Player player, @NotNull WarpData warpData) {
        super(player);
        this.warpData = Objects.requireNonNull(warpData, "warpData cannot be null");
    }

    public @NotNull WarpData getWarpData() {
        return warpData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        WarpTeleportEvent that = (WarpTeleportEvent) o;
        return Objects.equals(warpData, that.warpData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), warpData);
    }

    @Override
    public @NotNull String toString() {
        return "WarpTeleportEvent{" + "player=" + getPlayer().getName() + ", warpData=" + warpData + '}';
    }
}