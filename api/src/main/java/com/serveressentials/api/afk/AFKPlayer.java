package com.serveressentials.api.afk;

import java.util.UUID;

public class AFKPlayer {
    private final UUID playerId;
    private final boolean isAFK;
    private final long since;

    public AFKPlayer(UUID playerId, boolean isAFK, long since) {
        this.playerId = playerId;
        this.isAFK = isAFK;
        this.since = since;
    }

    public UUID getPlayerId() { return playerId; }
    public boolean isAFK() { return isAFK; }
    public long getSince() { return since; }
}