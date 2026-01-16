package com.serveressentials.api.report.event;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;


public class ReportEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final @Nullable Player player;

    public ReportEvent(@Nullable Player player) {
        super(!Bukkit.isPrimaryThread());
        this.player = player;
    }

    /**
     * Gets the player involved in this event (null for system/auto events).
     *
     * @return The player, or null
     */
    public @Nullable Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }
}