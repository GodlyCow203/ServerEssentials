package com.serveressentials.api.back.event;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;


public class BackEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final @NotNull Player player;
    private final @NotNull BackType backType;

    public enum BackType {
        BACK_LOCATION,
        LOBBY,
        DEATH_LOCATION
    }

    public BackEvent(@NotNull Player player, @NotNull BackType backType) {
        super(!Bukkit.isPrimaryThread());
        this.player = Objects.requireNonNull(player, "player cannot be null");
        this.backType = Objects.requireNonNull(backType, "backType cannot be null");
    }

    public @NotNull Player getPlayer() { return player; }
    public @NotNull BackType getBackType() { return backType; }

    @Override
    public @NotNull HandlerList getHandlers() { return handlers; }

    public static @NotNull HandlerList getHandlerList() { return handlers; }
}


