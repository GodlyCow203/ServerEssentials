package com.serveressentials.api.warp;

import org.jetbrains.annotations.NotNull;
import java.time.Duration;
import java.util.Objects;

public final class WarpSettings {
    private final @NotNull Duration cooldown;
    private final int defaultMaxWarps;

    public WarpSettings(@NotNull Duration cooldown, int defaultMaxWarps) {
        this.cooldown = Objects.requireNonNull(cooldown, "cooldown cannot be null");
        this.defaultMaxWarps = defaultMaxWarps;
    }

    public @NotNull Duration getCooldown() {
        return cooldown;
    }

    public int getDefaultMaxWarps() {
        return defaultMaxWarps;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WarpSettings that = (WarpSettings) o;
        return defaultMaxWarps == that.defaultMaxWarps && Objects.equals(cooldown, that.cooldown);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cooldown, defaultMaxWarps);
    }

    @Override
    public @NotNull String toString() {
        return "WarpSettings{" + "cooldown=" + cooldown + ", defaultMaxWarps=" + defaultMaxWarps + '}';
    }
}