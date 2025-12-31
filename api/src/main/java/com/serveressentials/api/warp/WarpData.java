package com.serveressentials.api.warp;

import org.jetbrains.annotations.NotNull;
import java.util.Objects;
import java.util.UUID;

public final class WarpData {
    private final @NotNull String name;
    private final @NotNull WarpLocation location;
    private final @NotNull UUID creator;

    public WarpData(@NotNull String name, @NotNull WarpLocation location, @NotNull UUID creator) {
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.location = Objects.requireNonNull(location, "location cannot be null");
        this.creator = Objects.requireNonNull(creator, "creator cannot be null");
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull WarpLocation getLocation() {
        return location;
    }

    public @NotNull UUID getCreator() {
        return creator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WarpData warpData = (WarpData) o;
        return Objects.equals(name, warpData.name) && Objects.equals(location, warpData.location) && Objects.equals(creator, warpData.creator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, location, creator);
    }

    @Override
    public @NotNull String toString() {
        return "WarpData{" + "name='" + name + '\'' + ", location=" + location + ", creator=" + creator + '}';
    }
}