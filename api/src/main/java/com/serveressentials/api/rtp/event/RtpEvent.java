package com.serveressentials.api.rtp.event;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;


public class RtpEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final @NotNull Player player;

    public RtpEvent(@NotNull Player player) {
        super(!Bukkit.isPrimaryThread());
        this.player = Objects.requireNonNull(player, "player cannot be null");
    }

    /**
     * Gets the player involved in this event.
     *
     * @return The player
     */
    public @NotNull Player getPlayer() {
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