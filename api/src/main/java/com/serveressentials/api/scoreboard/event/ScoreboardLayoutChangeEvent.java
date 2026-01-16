package com.serveressentials.api.scoreboard.event;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;

public final class ScoreboardLayoutChangeEvent extends ScoreboardEvent {
    private final @NotNull String newLayout;
    private final @Nullable String previousLayout;

    public ScoreboardLayoutChangeEvent(@NotNull Player player, @NotNull String newLayout, @Nullable String previousLayout) {
        super(player);
        this.newLayout = Objects.requireNonNull(newLayout, "newLayout cannot be null");
        this.previousLayout = previousLayout;
    }

    public @NotNull String getNewLayout() {
        return newLayout;
    }

    public @Nullable String getPreviousLayout() {
        return previousLayout;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ScoreboardLayoutChangeEvent that = (ScoreboardLayoutChangeEvent) o;
        return Objects.equals(newLayout, that.newLayout) && Objects.equals(previousLayout, that.previousLayout);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), newLayout, previousLayout);
    }

    @Override
    public @NotNull String toString() {
        return "ScoreboardLayoutChangeEvent{" + "player=" + getPlayer().getName() + ", newLayout='" + newLayout + '\'' + ", previousLayout='" + previousLayout + '\'' + '}';
    }
}