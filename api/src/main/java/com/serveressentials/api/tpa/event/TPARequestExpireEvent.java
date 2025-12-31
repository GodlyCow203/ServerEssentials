package com.serveressentials.api.tpa.event;

import com.serveressentials.api.tpa.TPARequestData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;

public final class TPARequestExpireEvent extends TPAEvent {
    private final @NotNull TPARequestData request;

    public TPARequestExpireEvent(@NotNull Player player, @NotNull TPARequestData request) {
        super(player);
        this.request = Objects.requireNonNull(request, "request cannot be null");
    }

    public @NotNull TPARequestData getRequest() {
        return request;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TPARequestExpireEvent that = (TPARequestExpireEvent) o;
        return Objects.equals(request, that.request);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), request);
    }

    @Override
    public @NotNull String toString() {
        return "TPARequestExpireEvent{" + "player=" + getPlayer().getName() + ", request=" + request + '}';
    }
}