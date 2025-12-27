package com.serveressentials.api.home;

import java.time.Instant;

public interface HomeMeta {

    Instant getCreatedAt();

    Instant getLastUsed();

    void setLastUsed(Instant instant);

    String getCreatorName();

    boolean isPublic();
}