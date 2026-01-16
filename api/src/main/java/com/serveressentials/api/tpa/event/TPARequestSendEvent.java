package com.serveressentials.api.tpa.event;

import com.serveressentials.api.tpa.TPARequestData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;

public final class TPARequestSendEvent extends TPAEvent {
    private final @NotNull TPARequestData request;

    public TPARequestSendEvent(@NotNull Player player, @NotNull TPARequestData request) {
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
        TPARequestSendEvent that = (TPARequestSendEvent) o;
        return Objects.equals(request, that.request);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), request);
    }

    @Override
    public @NotNull String toString() {
        return "TPARequestSendEvent{" + "player=" + getPlayer().getName() + ", request=" + request + '}';
    }
}