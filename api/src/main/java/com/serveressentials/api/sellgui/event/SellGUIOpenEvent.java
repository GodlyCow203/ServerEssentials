package com.serveressentials.api.sellgui.event;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;

public final class SellGUIOpenEvent extends SellGUIEvent {
    private final long openTime;

    public SellGUIOpenEvent(@NotNull Player player) {
        this(player, System.currentTimeMillis());
    }

    public SellGUIOpenEvent(@NotNull Player player, long openTime) {
        super(player);
        this.openTime = openTime;
    }

    public long getOpenTime() {
        return openTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SellGUIOpenEvent that = (SellGUIOpenEvent) o;
        return openTime == that.openTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), openTime);
    }

    @Override
    public @NotNull String toString() {
        return "SellGUIOpenEvent{" + "player=" + getPlayer().getName() + ", openTime=" + openTime + '}';
    }
}