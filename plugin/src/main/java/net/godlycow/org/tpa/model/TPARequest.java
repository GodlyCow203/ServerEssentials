package net.godlycow.org.tpa.model;

import java.util.UUID;

public class TPARequest {
    public final UUID senderId;
    public final UUID targetId;
    public final boolean here;
    public final double cost;
    public final long timestamp;
    public boolean inWarmup = false;

    public TPARequest(UUID senderId, UUID targetId, boolean here, double cost, long timestamp) {
        this.senderId = senderId;
        this.targetId = targetId;
        this.here = here;
        this.cost = cost;
        this.timestamp = timestamp;
    }

    public static TPARequest create(UUID senderId, UUID targetId, boolean here, double cost) {
        return new TPARequest(senderId, targetId, here, cost, System.currentTimeMillis());
    }
}