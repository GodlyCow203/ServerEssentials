package com.serveressentials.api.back.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class BackLocationSaveEvent extends BackEvent {
    private final @NotNull Location savedLocation;

    public BackLocationSaveEvent(@NotNull Player player, @NotNull Location savedLocation) {
        super(player, BackType.BACK_LOCATION);
        this.savedLocation = Objects.requireNonNull(savedLocation, "savedLocation cannot be null").clone();
    }

    public @NotNull Location getSavedLocation() { return savedLocation.clone(); }
}
