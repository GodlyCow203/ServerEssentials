package com.serveressentials.api.home.cooldown;

import java.util.UUID;

public interface CooldownManager {

    boolean isOnCooldown(UUID playerId, CooldownType type);

    long getRemainingTime(UUID playerId, CooldownType type);

    void setCooldown(UUID playerId, CooldownType type, long seconds);

    void clearCooldown(UUID playerId, CooldownType type);

    enum CooldownType {
        SET, TELEPORT
    }
}