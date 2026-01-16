package com.serveressentials.api.economy.event;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;


public class EconomyEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final @NotNull OfflinePlayer player;

    public EconomyEvent(@NotNull OfflinePlayer player) {
        super(!Bukkit.isPrimaryThread());
        this.player = Objects.requireNonNull(player, "player cannot be null");
    }

    public @NotNull OfflinePlayer getPlayer() { return player; }

    @Override
    public @NotNull HandlerList getHandlers() { return handlers; }

    public static @NotNull HandlerList getHandlerList() { return handlers; }
}