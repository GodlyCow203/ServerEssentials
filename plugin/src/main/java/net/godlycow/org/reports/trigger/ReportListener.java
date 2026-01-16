package net.godlycow.org.reports.trigger;

import net.godlycow.org.commands.impl.ReportCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ReportListener implements Listener {
    private final ReportCommand reportCommand;

    public ReportListener(ReportCommand reportCommand) {
        this.reportCommand = reportCommand;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        reportCommand.handleJoin(event.getPlayer());
    }
}