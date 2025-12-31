package com.serveressentials.api.report;

import org.jetbrains.annotations.NotNull;
import java.util.Objects;

public final class ReportStats {
    private final int totalReports;
    private final int pendingReports;
    private final int clearedReports;
    private final long oldestReportTimestamp;
    private final long newestReportTimestamp;

    public ReportStats(int totalReports, int pendingReports, int clearedReports,
                       long oldestReportTimestamp, long newestReportTimestamp) {
        this.totalReports = totalReports;
        this.pendingReports = pendingReports;
        this.clearedReports = clearedReports;
        this.oldestReportTimestamp = oldestReportTimestamp;
        this.newestReportTimestamp = newestReportTimestamp;
    }

    public int getTotalReports() {
        return totalReports;
    }

    public int getPendingReports() {
        return pendingReports;
    }

    public int getClearedReports() {
        return clearedReports;
    }

    public long getOldestReportTimestamp() {
        return oldestReportTimestamp;
    }

    public long getNewestReportTimestamp() {
        return newestReportTimestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ReportStats)) return false;
        ReportStats that = (ReportStats) obj;
        return totalReports == that.totalReports &&
                pendingReports == that.pendingReports &&
                clearedReports == that.clearedReports &&
                oldestReportTimestamp == that.oldestReportTimestamp &&
                newestReportTimestamp == that.newestReportTimestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalReports, pendingReports, clearedReports, oldestReportTimestamp, newestReportTimestamp);
    }

    @Override
    public String toString() {
        return "ReportStats{" +
                "totalReports=" + totalReports +
                ", pendingReports=" + pendingReports +
                ", clearedReports=" + clearedReports +
                ", oldestReportTimestamp=" + oldestReportTimestamp +
                ", newestReportTimestamp=" + newestReportTimestamp +
                '}';
    }
}