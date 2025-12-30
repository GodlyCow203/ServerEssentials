package com.serveressentials.api.nick;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;
import java.util.UUID;


public final class NickInfo {
    private final @NotNull UUID playerId;
    private final @NotNull String playerName;
    private final @NotNull String nickname;
    private final long timestamp;
    private final int changesToday;

    public NickInfo(@NotNull UUID playerId, @NotNull String playerName, @NotNull String nickname,
                    long timestamp, int changesToday) {
        this.playerId = Objects.requireNonNull(playerId, "playerId cannot be null");
        this.playerName = Objects.requireNonNull(playerName, "playerName cannot be null");
        this.nickname = Objects.requireNonNull(nickname, "nickname cannot be null");
        this.timestamp = timestamp;
        this.changesToday = changesToday;
    }

    public @NotNull UUID getPlayerId() {
        return playerId;
    }

    public @NotNull String getPlayerName() {
        return playerName;
    }

    public @NotNull String getNickname() {
        return nickname;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getChangesToday() {
        return changesToday;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NickInfo)) return false;
        NickInfo that = (NickInfo) obj;
        return timestamp == that.timestamp &&
                changesToday == that.changesToday &&
                playerId.equals(that.playerId) &&
                playerName.equals(that.playerName) &&
                nickname.equals(that.nickname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, playerName, nickname, timestamp, changesToday);
    }

    @Override
    public String toString() {
        return "NickInfo{" +
                "playerId=" + playerId +
                ", playerName='" + playerName + '\'' +
                ", nickname='" + nickname + '\'' +
                ", timestamp=" + timestamp +
                ", changesToday=" + changesToday +
                '}';
    }
}