package com.serveressentials.api.scoreboard;

import org.jetbrains.annotations.Nullable;
import java.util.Objects;

public final class ScoreboardWorldSetting {
    private final boolean enabled;
    private final @Nullable String layout;

    public ScoreboardWorldSetting(boolean enabled, @Nullable String layout) {
        this.enabled = enabled;
        this.layout = layout;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public @Nullable String getLayout() {
        return layout;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScoreboardWorldSetting that = (ScoreboardWorldSetting) o;
        return enabled == that.enabled && Objects.equals(layout, that.layout);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, layout);
    }

    @Override
    public String toString() {
        return "ScoreboardWorldSetting{" + "enabled=" + enabled + ", layout='" + layout + '\'' + '}';
    }
}