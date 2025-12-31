package com.serveressentials.api.warp.event;

import com.serveressentials.api.warp.WarpData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;
import java.util.UUID;

public final class WarpDeleteEvent extends WarpEvent {
    private final @NotNull String warpName;
    private final @Nullable UUID deletedBy;

    public WarpDeleteEvent(@NotNull Player player, @NotNull String warpName, @Nullable UUID deletedBy) {
        super(player);
        this.warpName = Objects.requireNonNull(warpName, "warpName cannot be null");
        this.deletedBy = deletedBy;
    }

    public @NotNull String getWarpName() {
        return warpName;
    }

    public @Nullable UUID getDeletedBy() {
        return deletedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        WarpDeleteEvent that = (WarpDeleteEvent) o;
        return Objects.equals(warpName, that.warpName) && Objects.equals(deletedBy, that.deletedBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), warpName, deletedBy);
    }

    @Override
    public @NotNull String toString() {
        return "WarpDeleteEvent{" + "player=" + getPlayer().getName() + ", warpName='" + warpName + '\'' + ", deletedBy=" + deletedBy + '}';
    }
}