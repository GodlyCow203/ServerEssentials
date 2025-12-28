package com.serveressentials.api.daily.event;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DailyGUIOpenEvent extends DailyEvent {
    private final int page;

    public DailyGUIOpenEvent(@NotNull Player player, int page) {
        super(player);
        this.page = page;
    }

    public int getPage() { return page; }
}