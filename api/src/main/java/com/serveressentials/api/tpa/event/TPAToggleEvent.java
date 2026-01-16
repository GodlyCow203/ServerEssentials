package com.serveressentials.api.tpa.event;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;

public final class TPAToggleEvent extends TPAEvent {
    private final boolean newState;

    public TPAToggleEvent(@NotNull Player player, boolean newState) {
        super(player);
        this.newState = newState;
    }

    public boolean getNewState() {
        return newState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TPAToggleEvent that = (TPAToggleEvent) o;
        return newState == that.newState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), newState);
    }

    @Override
    public @NotNull String toString() {
        return "TPAToggleEvent{" + "player=" + getPlayer().getName() + ", newState=" + newState + '}';
    }
}