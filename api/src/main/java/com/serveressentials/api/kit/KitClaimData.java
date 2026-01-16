package com.serveressentials.api.kit;

import org.jetbrains.annotations.NotNull;
import java.util.Objects;


public final class KitClaimData {
    private final long lastClaimed;
    private final int claimCount;

    public KitClaimData(long lastClaimed, int claimCount) {
        this.lastClaimed = lastClaimed;
        this.claimCount = claimCount;
    }

    public long getLastClaimed() {
        return lastClaimed;
    }

    public int getClaimCount() {
        return claimCount;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof KitClaimData)) return false;
        KitClaimData that = (KitClaimData) obj;
        return lastClaimed == that.lastClaimed && claimCount == that.claimCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lastClaimed, claimCount);
    }

    @Override
    public String toString() {
        return "KitClaimData{" +
                "lastClaimed=" + lastClaimed +
                ", claimCount=" + claimCount +
                '}';
    }
}