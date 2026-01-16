package com.serveressentials.api.kit.event;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;


public final class KitOpenGUIEvent extends KitEvent {
    private final int kitCount;

    public KitOpenGUIEvent(@NotNull Player player, int kitCount) {
        super(player);
        this.kitCount = kitCount;
    }

    /**
     * Gets the number of kits available in the GUI.
     *
     * @return The kit count
     */
    public int getKitCount() {
        return kitCount;
    }
}