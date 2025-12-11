package net.lunark.io.reports;

import org.bukkit.plugin.Plugin;

public class ReportConfig {
    private final Plugin plugin;

    public int cooldown;
    public int maxReasonLength;
    public int autoClearAfterDays;
    public boolean notifyOnlineOnly;
    public int joinNotifyDelay;

    public ReportConfig(Plugin plugin) {
        this.plugin = plugin;

         plugin.getConfig().addDefault("reports.cooldown", 60);
        plugin.getConfig().addDefault("reports.max-reason-length", 200);
        plugin.getConfig().addDefault("reports.auto-clear-after-days", 7);
        plugin.getConfig().addDefault("reports.notify-online-only", false);
        plugin.getConfig().addDefault("reports.join-notify-delay", 40);
        plugin.saveConfig();

        load();
    }

    private void load() {
        this.cooldown = plugin.getConfig().getInt("reports.cooldown", 60);
        this.maxReasonLength = plugin.getConfig().getInt("reports.max-reason-length", 200);
        this.autoClearAfterDays = plugin.getConfig().getInt("reports.auto-clear-after-days", 7);
        this.notifyOnlineOnly = plugin.getConfig().getBoolean("reports.notify-online-only", false);
        this.joinNotifyDelay = plugin.getConfig().getInt("reports.join-notify-delay", 40);
    }

    public void reload() {
        plugin.reloadConfig();
        load();
    }
}