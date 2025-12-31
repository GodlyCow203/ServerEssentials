package com.serveressentials.api.rtp;

import org.jetbrains.annotations.NotNull;
import java.util.Objects;

public final class RtpWorldConfig {
    private final @NotNull String worldName;
    private final boolean enabled;
    private final int minRadius;
    private final int maxRadius;
    private final int cooldownSeconds;

    public RtpWorldConfig(@NotNull String worldName, boolean enabled, int minRadius,
                          int maxRadius, int cooldownSeconds) {
        this.worldName = Objects.requireNonNull(worldName, "worldName cannot be null");
        this.enabled = enabled;
        this.minRadius = minRadius;
        this.maxRadius = maxRadius;
        this.cooldownSeconds = cooldownSeconds;
    }

    public @NotNull String getWorldName() {
        return worldName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getMinRadius() {
        return minRadius;
    }

    public int getMaxRadius() {
        return maxRadius;
    }

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof RtpWorldConfig)) return false;
        RtpWorldConfig that = (RtpWorldConfig) obj;
        return enabled == that.enabled &&
                minRadius == that.minRadius &&
                maxRadius == that.maxRadius &&
                cooldownSeconds == that.cooldownSeconds &&
                worldName.equals(that.worldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldName, enabled, minRadius, maxRadius, cooldownSeconds);
    }

    @Override
    public String toString() {
        return "RtpWorldConfig{" +
                "worldName='" + worldName + '\'' +
                ", enabled=" + enabled +
                ", minRadius=" + minRadius +
                ", maxRadius=" + maxRadius +
                ", cooldownSeconds=" + cooldownSeconds +
                '}';
    }
}