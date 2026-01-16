package com.serveressentials.api.report.event;

import com.serveressentials.api.report.ReportInfo;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;

public final class ReportCreateEvent extends ReportEvent {
    private final @NotNull ReportInfo reportInfo;

    public ReportCreateEvent(@NotNull Player reporter, @NotNull ReportInfo reportInfo) {
        super(reporter);
        this.reportInfo = Objects.requireNonNull(reportInfo, "reportInfo cannot be null");
    }

    /**
     * Gets the report that was created.
     *
     * @return The report info
     */
    public @NotNull ReportInfo getReportInfo() {
        return reportInfo;
    }
}