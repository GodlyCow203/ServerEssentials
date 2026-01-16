package com.serveressentials.api.scoreboard.event;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;

public final class ScoreboardToggleEvent extends ScoreboardEvent {
    private final boolean newState;

    public ScoreboardToggleEvent(@NotNull Player player, boolean newState) {
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
        ScoreboardToggleEvent that = (ScoreboardToggleEvent) o;
        return newState == that.newState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), newState);
    }

    @Override
    public @NotNull String toString() {
        return "ScoreboardToggleEvent{" + "player=" + getPlayer().getName() + ", newState=" + newState + '}';
    }
}