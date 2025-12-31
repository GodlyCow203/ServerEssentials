package com.serveressentials.api.scoreboard.event;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;

public final class ScoreboardReloadEvent extends ScoreboardEvent {
    private final long reloadTime;

    public ScoreboardReloadEvent(@NotNull Player player, long reloadTime) {
        super(player);
        this.reloadTime = reloadTime;
    }

    public ScoreboardReloadEvent(@NotNull Player player) {
        this(player, System.currentTimeMillis());
    }

    public long getReloadTime() {
        return reloadTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ScoreboardReloadEvent that = (ScoreboardReloadEvent) o;
        return reloadTime == that.reloadTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), reloadTime);
    }

    @Override
    public @NotNull String toString() {
        return "ScoreboardReloadEvent{" + "player=" + getPlayer().getName() + ", reloadTime=" + reloadTime + '}';
    }
}