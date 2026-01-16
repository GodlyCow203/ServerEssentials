package com.serveressentials.api.scoreboard;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;

public final class ScoreboardPlayerData {
    private final boolean enabled;
    private final @Nullable String layout;
    private final long lastUpdate;
    private final long joinTime;

    public ScoreboardPlayerData(boolean enabled, @Nullable String layout, long lastUpdate, long joinTime) {
        this.enabled = enabled;
        this.layout = layout;
        this.lastUpdate = lastUpdate;
        this.joinTime = joinTime;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public @Nullable String getLayout() {
        return layout;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public long getJoinTime() {
        return joinTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScoreboardPlayerData that = (ScoreboardPlayerData) o;
        return enabled == that.enabled && lastUpdate == that.lastUpdate && joinTime == that.joinTime && Objects.equals(layout, that.layout);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, layout, lastUpdate, joinTime);
    }

    @Override
    public @NotNull String toString() {
        return "ScoreboardPlayerData{" + "enabled=" + enabled + ", layout='" + layout + '\'' + ", lastUpdate=" + lastUpdate + ", joinTime=" + joinTime + '}';
    }
}