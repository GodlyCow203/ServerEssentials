package com.serveressentials.api.mail;

import org.jetbrains.annotations.NotNull;
import java.util.Objects;
import java.util.UUID;


public final class MailStats {
    private final @NotNull UUID playerId;
    private final int totalCount;
    private final int unreadCount;
    private final long lastActivity;

    public MailStats(@NotNull UUID playerId, int totalCount, int unreadCount, long lastActivity) {
        this.playerId = Objects.requireNonNull(playerId, "playerId cannot be null");
        this.totalCount = totalCount;
        this.unreadCount = unreadCount;
        this.lastActivity = lastActivity;
    }

    public @NotNull UUID getPlayerId() {
        return playerId;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public long getLastActivity() {
        return lastActivity;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MailStats)) return false;
        MailStats that = (MailStats) obj;
        return totalCount == that.totalCount &&
                unreadCount == that.unreadCount &&
                lastActivity == that.lastActivity &&
                playerId.equals(that.playerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, totalCount, unreadCount, lastActivity);
    }

    @Override
    public String toString() {
        return "MailStats{" +
                "playerId=" + playerId +
                ", totalCount=" + totalCount +
                ", unreadCount=" + unreadCount +
                ", lastActivity=" + lastActivity +
                '}';
    }
}