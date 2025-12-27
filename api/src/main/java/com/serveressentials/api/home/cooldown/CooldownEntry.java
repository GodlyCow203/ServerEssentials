package com.serveressentials.api.home.cooldown;

import java.time.Instant;

public interface CooldownEntry {

    Instant getStartTime();

    long getDurationSeconds();

    boolean isExpired();
}