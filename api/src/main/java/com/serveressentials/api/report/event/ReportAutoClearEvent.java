package com.serveressentials.api.report.event;

import com.serveressentials.api.report.ReportInfo;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Objects;

public final class ReportAutoClearEvent extends ReportEvent {
    private final @NotNull List<ReportInfo> clearedReports;

    public ReportAutoClearEvent(@NotNull List<ReportInfo> clearedReports) {
        super(null);
        this.clearedReports = Objects.requireNonNull(clearedReports, "clearedReports cannot be null");
    }

    /**
     * Gets the list of reports that were auto-cleared.
     *
     * @return The list of cleared report info
     */
    public @NotNull List<ReportInfo> getClearedReports() {
        return clearedReports;
    }
}