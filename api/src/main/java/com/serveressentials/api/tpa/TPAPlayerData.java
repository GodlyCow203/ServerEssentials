package com.serveressentials.api.tpa;

import org.jetbrains.annotations.NotNull;
import java.util.Objects;
import java.util.UUID;

public final class TPAPlayerData {
    private final @NotNull UUID playerId;
    private final long cooldownTime;
    private final boolean requestsDisabled;
    private final int activeRequestsSent;
    private final int activeRequestsReceived;

    public TPAPlayerData(@NotNull UUID playerId, long cooldownTime, boolean requestsDisabled,
                         int activeRequestsSent, int activeRequestsReceived) {
        this.playerId = Objects.requireNonNull(playerId, "playerId cannot be null");
        this.cooldownTime = cooldownTime;
        this.requestsDisabled = requestsDisabled;
        this.activeRequestsSent = activeRequestsSent;
        this.activeRequestsReceived = activeRequestsReceived;
    }

    public @NotNull UUID getPlayerId() {
        return playerId;
    }

    public long getCooldownTime() {
        return cooldownTime;
    }

    public boolean isRequestsDisabled() {
        return requestsDisabled;
    }

    public int getActiveRequestsSent() {
        return activeRequestsSent;
    }

    public int getActiveRequestsReceived() {
        return activeRequestsReceived;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TPAPlayerData that = (TPAPlayerData) o;
        return cooldownTime == that.cooldownTime && requestsDisabled == that.requestsDisabled &&
                activeRequestsSent == that.activeRequestsSent && activeRequestsReceived == that.activeRequestsReceived &&
                Objects.equals(playerId, that.playerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, cooldownTime, requestsDisabled, activeRequestsSent, activeRequestsReceived);
    }

    @Override
    public @NotNull String toString() {
        return "TPAPlayerData{" + "playerId=" + playerId + ", cooldownTime=" + cooldownTime +
                ", requestsDisabled=" + requestsDisabled + ", activeRequestsSent=" + activeRequestsSent +
                ", activeRequestsReceived=" + activeRequestsReceived + '}';
    }
}