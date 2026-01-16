package com.serveressentials.api.back;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;


public final class BackLocationData {

    private final @NotNull String worldName;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;
    private final long timestamp;

    public BackLocationData(
            @NotNull String worldName,
            double x,
            double y,
            double z,
            float yaw,
            float pitch,
            long timestamp
    ) {
        this.worldName = Objects.requireNonNull(worldName, "worldName cannot be null");
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.timestamp = timestamp;
    }

    public BackLocationData(@NotNull Location location) {
        this(
                Objects.requireNonNull(location.getWorld(), "World cannot be null").getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch(),
                System.currentTimeMillis()
        );
    }

    public @NotNull String getWorldName() {
        return worldName;
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

    public long getTimestamp() {
        return timestamp;
    }


    public @Nullable Location toLocation() {
        World world = Bukkit.getWorld(worldName);
        return world != null ? new Location(world, x, y, z, yaw, pitch) : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BackLocationData that)) return false;

        return Double.compare(that.x, x) == 0
                && Double.compare(that.y, y) == 0
                && Double.compare(that.z, z) == 0
                && Float.compare(that.yaw, yaw) == 0
                && Float.compare(that.pitch, pitch) == 0
                && timestamp == that.timestamp
                && worldName.equals(that.worldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldName, x, y, z, yaw, pitch, timestamp);
    }

    @Override
    public String toString() {
        return "BackLocationData{" +
                "worldName='" + worldName + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", yaw=" + yaw +
                ", pitch=" + pitch +
                ", timestamp=" + timestamp +
                '}';
    }
}
