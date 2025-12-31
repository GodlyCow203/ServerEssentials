package com.serveressentials.api.report;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;
import java.util.UUID;


public final class ReportInfo {
    private final @NotNull String id;
    private final @NotNull UUID reporterId;
    private final @NotNull UUID targetId;
    private final @NotNull String reason;
    private final long timestamp;
    private final boolean pending;
    private final @Nullable String reporterName;
    private final @Nullable String targetName;

    public ReportInfo(@NotNull String id, @NotNull UUID reporterId, @NotNull UUID targetId,
                      @NotNull String reason, long timestamp, boolean pending,
                      @Nullable String reporterName, @Nullable String targetName) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.reporterId = Objects.requireNonNull(reporterId, "reporterId cannot be null");
        this.targetId = Objects.requireNonNull(targetId, "targetId cannot be null");
        this.reason = Objects.requireNonNull(reason, "reason cannot be null");
        this.timestamp = timestamp;
        this.pending = pending;
        this.reporterName = reporterName;
        this.targetName = targetName;
    }

    public @NotNull String getId() {
        return id;
    }

    public @NotNull UUID getReporterId() {
        return reporterId;
    }

    public @NotNull UUID getTargetId() {
        return targetId;
    }

    public @NotNull String getReason() {
        return reason;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isPending() {
        return pending;
    }

    public @Nullable String getReporterName() {
        return reporterName;
    }

    public @Nullable String getTargetName() {
        return targetName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ReportInfo)) return false;
        ReportInfo that = (ReportInfo) obj;
        return timestamp == that.timestamp &&
                pending == that.pending &&
                id.equals(that.id) &&
                reporterId.equals(that.reporterId) &&
                targetId.equals(that.targetId) &&
                reason.equals(that.reason) &&
                Objects.equals(reporterName, that.reporterName) &&
                Objects.equals(targetName, that.targetName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reporterId, targetId, reason, timestamp, pending, reporterName, targetName);
    }

    @Override
    public String toString() {
        return "ReportInfo{" +
                "id='" + id + '\'' +
                ", reporterId=" + reporterId +
                ", targetId=" + targetId +
                ", reason='" + reason + '\'' +
                ", timestamp=" + timestamp +
                ", pending=" + pending +
                ", reporterName='" + reporterName + '\'' +
                ", targetName='" + targetName + '\'' +
                '}';
    }
}