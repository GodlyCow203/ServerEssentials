package com.serveressentials.api.daily.event;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;


public class DailyEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final @NotNull Player player;

    public DailyEvent(@NotNull Player player) {
        super(!Bukkit.isPrimaryThread());
        this.player = Objects.requireNonNull(player, "player cannot be null");
    }

    public @NotNull Player getPlayer() { return player; }

    @Override
    public @NotNull HandlerList getHandlers() { return handlers; }

    public static @NotNull HandlerList getHandlerList() { return handlers; }
}