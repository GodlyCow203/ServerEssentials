package com.serveressentials.api.mail;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;
import java.util.UUID;


public final class MailInfo {
    private final @NotNull UUID senderId;
    private final @NotNull String senderName;
    private final @NotNull String message;
    private final long timestamp;
    private final @Nullable UUID targetId;
    private final boolean read;

    public MailInfo(@NotNull UUID senderId, @NotNull String senderName, @NotNull String message,
                    long timestamp, @Nullable UUID targetId, boolean read) {
        this.senderId = Objects.requireNonNull(senderId, "senderId cannot be null");
        this.senderName = Objects.requireNonNull(senderName, "senderName cannot be null");
        this.message = Objects.requireNonNull(message, "message cannot be null");
        this.timestamp = timestamp;
        this.targetId = targetId;
        this.read = read;
    }

    public @NotNull UUID getSenderId() {
        return senderId;
    }

    public @NotNull String getSenderName() {
        return senderName;
    }

    public @NotNull String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public @Nullable UUID getTargetId() {
        return targetId;
    }

    public boolean isRead() {
        return read;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MailInfo)) return false;
        MailInfo that = (MailInfo) obj;
        return timestamp == that.timestamp &&
                read == that.read &&
                senderId.equals(that.senderId) &&
                senderName.equals(that.senderName) &&
                message.equals(that.message) &&
                Objects.equals(targetId, that.targetId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(senderId, senderName, message, timestamp, targetId, read);
    }

    @Override
    public String toString() {
        return "MailInfo{" +
                "senderId=" + senderId +
                ", senderName='" + senderName + '\'' +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                ", targetId=" + targetId +
                ", read=" + read +
                '}';
    }
}