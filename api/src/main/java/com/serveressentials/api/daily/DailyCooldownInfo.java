package com.serveressentials.api.daily;

import org.jetbrains.annotations.NotNull;

public final class DailyCooldownInfo {
    private final long hours;
    private final long minutes;
    private final long seconds;
    private final boolean onCooldown;

    public DailyCooldownInfo(long hours, long minutes, long seconds, boolean onCooldown) {
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.onCooldown = onCooldown;
    }

    public long getHours() { return hours; }
    public long getMinutes() { return minutes; }
    public long getSeconds() { return seconds; }
    public boolean isOnCooldown() { return onCooldown; }


    public @NotNull String format() {
        return String.format("%02dh %02dm %02ds", hours, minutes, seconds);
    }

    @Override
    public String toString() {
        return "DailyCooldownInfo{" + "hours=" + hours + ", minutes=" + minutes +
                ", seconds=" + seconds + ", onCooldown=" + onCooldown + '}';
    }
}