package net.godlycow.org.reports.model;

import java.util.UUID;

public record Report(
        String id,
        UUID reporterId,
        UUID targetId,
        String reason,
        long timestamp,
        boolean pending
) {}