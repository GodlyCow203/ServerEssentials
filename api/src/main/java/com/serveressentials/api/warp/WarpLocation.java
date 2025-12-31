package com.serveressentials.api.warp;

import org.jetbrains.annotations.NotNull;
import java.util.Objects;

public final class WarpLocation {
    private final @NotNull String world;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;

    public WarpLocation(@NotNull String world, double x, double y, double z, float yaw, float pitch) {
        this.world = Objects.requireNonNull(world, "world cannot be null");
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public @NotNull String getWorld() {
        return world;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WarpLocation that = (WarpLocation) o;
        return Double.compare(that.x, x) == 0 && Double.compare(that.y, y) == 0 &&
                Double.compare(that.z, z) == 0 && Float.compare(that.yaw, yaw) == 0 &&
                Float.compare(that.pitch, pitch) == 0 && Objects.equals(world, that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, y, z, yaw, pitch);
    }

    @Override
    public @NotNull String toString() {
        return "WarpLocation{" + "world='" + world + '\'' + ", x=" + x + ", y=" + y +
                ", z=" + z + ", yaw=" + yaw + ", pitch=" + pitch + '}';
    }
}