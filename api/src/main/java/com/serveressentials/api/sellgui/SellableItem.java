package com.serveressentials.api.sellgui;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;

public final class SellableItem {
    private final @NotNull Material material;
    private final double price;

    public SellableItem(@NotNull Material material, double price) {
        this.material = Objects.requireNonNull(material, "material cannot be null");
        this.price = price;
    }

    public @NotNull Material getMaterial() {
        return material;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SellableItem that = (SellableItem) o;
        return Double.compare(that.price, price) == 0 && material == that.material;
    }

    @Override
    public int hashCode() {
        return Objects.hash(material, price);
    }

    @Override
    public @NotNull String toString() {
        return "SellableItem{" + "material=" + material + ", price=" + price + '}';
    }
}