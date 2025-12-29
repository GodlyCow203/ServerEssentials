package com.serveressentials.api.kit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Objects;


public final class KitInfo {
    private final @NotNull String id;
    private final @NotNull String name;
    private final @Nullable String permission;
    private final @NotNull String displayName;
    private final @NotNull String displayMaterial;
    private final @NotNull List<String> displayLore;
    private final int slot;
    private final int cooldown;
    private final @NotNull List<KitItem> items;

    public KitInfo(
            @NotNull String id,
            @NotNull String name,
            @Nullable String permission,
            @NotNull String displayName,
            @NotNull String displayMaterial,
            @NotNull List<String> displayLore,
            int slot,
            int cooldown,
            @NotNull List<KitItem> items
    ) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.permission = permission;
        this.displayName = Objects.requireNonNull(displayName, "displayName cannot be null");
        this.displayMaterial = Objects.requireNonNull(displayMaterial, "displayMaterial cannot be null");
        this.displayLore = Objects.requireNonNull(displayLore, "displayLore cannot be null");
        this.slot = slot;
        this.cooldown = cooldown;
        this.items = Objects.requireNonNull(items, "items cannot be null");
    }

    public @NotNull String getId() {
        return id;
    }

    public @NotNull String getName() {
        return name;
    }

    public @Nullable String getPermission() {
        return permission;
    }

    public @NotNull String getDisplayName() {
        return displayName;
    }

    public @NotNull String getDisplayMaterial() {
        return displayMaterial;
    }

    public @NotNull List<String> getDisplayLore() {
        return displayLore;
    }

    public int getSlot() {
        return slot;
    }

    public int getCooldown() {
        return cooldown;
    }

    public @NotNull List<KitItem> getItems() {
        return items;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof KitInfo)) return false;
        KitInfo that = (KitInfo) obj;
        return slot == that.slot &&
                cooldown == that.cooldown &&
                id.equals(that.id) &&
                name.equals(that.name) &&
                Objects.equals(permission, that.permission) &&
                displayName.equals(that.displayName) &&
                displayMaterial.equals(that.displayMaterial) &&
                displayLore.equals(that.displayLore) &&
                items.equals(that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, permission, displayName, displayMaterial, displayLore, slot, cooldown, items);
    }

    @Override
    public String toString() {
        return "KitInfo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", permission='" + permission + '\'' +
                ", displayName='" + displayName + '\'' +
                ", displayMaterial='" + displayMaterial + '\'' +
                ", displayLore=" + displayLore +
                ", slot=" + slot +
                ", cooldown=" + cooldown +
                ", items=" + items +
                '}';
    }
}