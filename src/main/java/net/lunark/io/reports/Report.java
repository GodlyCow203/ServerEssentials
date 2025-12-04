package net.lunark.io.reports;

import java.util.UUID;

public record Report(
        String id,
        UUID reporterId,
        UUID targetId,
        String reason,
        long timestamp,
        boolean pending
) {}