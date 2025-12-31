package com.serveressentials.api.report.event;

import com.serveressentials.api.report.ReportInfo;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;


public final class ReportClearEvent extends ReportEvent {
    private final @NotNull ReportInfo reportInfo;
    private final boolean isManualClear;

    public ReportClearEvent(@Nullable Player clearer, @NotNull ReportInfo reportInfo, boolean isManualClear) {
        super(clearer);
        this.reportInfo = Objects.requireNonNull(reportInfo, "reportInfo cannot be null");
        this.isManualClear = isManualClear;
    }

    /**
     * Gets the report that was cleared.
     *
     * @return The report info
     */
    public @NotNull ReportInfo getReportInfo() {
        return reportInfo;
    }

    /**
     * Checks if this was a manual clear (true) or auto-clear (false).
     *
     * @return true if manually cleared, false if auto-cleared
     */
    public boolean isManualClear() {
        return isManualClear;
    }
}