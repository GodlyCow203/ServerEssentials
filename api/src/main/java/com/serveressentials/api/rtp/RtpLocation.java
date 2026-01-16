package com.serveressentials.api.rtp;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;
import java.util.UUID;

public final class RtpLocation {
    private final @NotNull UUID playerId;
    private final @NotNull String playerName;
    private final @NotNull String worldName;
    private final double x, y, z;
    private final long timestamp;

    public RtpLocation(@NotNull UUID playerId, @NotNull String playerName,
                       @NotNull String worldName, double x, double y, double z, long timestamp) {
        this.playerId = Objects.requireNonNull(playerId, "playerId cannot be null");
        this.playerName = Objects.requireNonNull(playerName, "playerName cannot be null");
        this.worldName = Objects.requireNonNull(worldName, "worldName cannot be null");
        this.x = x;
        this.y = y;
        this.z = z;
        this.timestamp = timestamp;
    }

    public @NotNull UUID getPlayerId() {
        return playerId;
    }

    public @NotNull String getPlayerName() {
        return playerName;
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

    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Converts this RTP location to a Bukkit Location.
     *
     * @param world The world object
     * @return The Bukkit Location
     */
    public Location toLocation(@NotNull World world) {
        return new Location(world, x, y, z);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof RtpLocation)) return false;
        RtpLocation that = (RtpLocation) obj;
        return Double.compare(that.x, x) == 0 &&
                Double.compare(that.y, y) == 0 &&
                Double.compare(that.z, z) == 0 &&
                timestamp == that.timestamp &&
                playerId.equals(that.playerId) &&
                playerName.equals(that.playerName) &&
                worldName.equals(that.worldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, playerName, worldName, x, y, z, timestamp);
    }

    @Override
    public String toString() {
        return "RtpLocation{" +
                "playerId=" + playerId +
                ", playerName='" + playerName + '\'' +
                ", worldName='" + worldName + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", timestamp=" + timestamp +
                '}';
    }
}