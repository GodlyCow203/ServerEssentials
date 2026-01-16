package com.serveressentials.api.tpa;

import org.jetbrains.annotations.NotNull;
import java.util.Objects;
import java.util.UUID;

public final class TPARequestData {
    private final @NotNull UUID senderId;
    private final @NotNull UUID targetId;
    private final boolean here;
    private final double cost;
    private final long timestamp;

    public TPARequestData(@NotNull UUID senderId, @NotNull UUID targetId, boolean here, double cost, long timestamp) {
        this.senderId = Objects.requireNonNull(senderId, "senderId cannot be null");
        this.targetId = Objects.requireNonNull(targetId, "targetId cannot be null");
        this.here = here;
        this.cost = cost;
        this.timestamp = timestamp;
    }

    public @NotNull UUID getSenderId() {
        return senderId;
    }

    public @NotNull UUID getTargetId() {
        return targetId;
    }

    public boolean isHere() {
        return here;
    }

    public double getCost() {
        return cost;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TPARequestData that = (TPARequestData) o;
        return here == that.here && Double.compare(that.cost, cost) == 0 &&
                timestamp == that.timestamp && Objects.equals(senderId, that.senderId) &&
                Objects.equals(targetId, that.targetId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(senderId, targetId, here, cost, timestamp);
    }

    @Override
    public @NotNull String toString() {
        return "TPARequestData{" + "senderId=" + senderId + ", targetId=" + targetId +
                ", here=" + here + ", cost=" + cost + ", timestamp=" + timestamp + '}';
    }
}