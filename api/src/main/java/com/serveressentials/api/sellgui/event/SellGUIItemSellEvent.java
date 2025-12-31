package com.serveressentials.api.sellgui.event;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;

public final class SellGUIItemSellEvent extends SellGUIEvent {
    private final @NotNull Material material;
    private final int quantity;
    private final double pricePerItem;
    private final double totalPrice;

    public SellGUIItemSellEvent(@NotNull Player player, @NotNull Material material, int quantity,
                                double pricePerItem, double totalPrice) {
        super(player);
        this.material = Objects.requireNonNull(material, "material cannot be null");
        this.quantity = quantity;
        this.pricePerItem = pricePerItem;
        this.totalPrice = totalPrice;
    }

    public @NotNull Material getMaterial() {
        return material;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPricePerItem() {
        return pricePerItem;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SellGUIItemSellEvent that = (SellGUIItemSellEvent) o;
        return quantity == that.quantity && Double.compare(that.pricePerItem, pricePerItem) == 0 &&
                Double.compare(that.totalPrice, totalPrice) == 0 && material == that.material;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), material, quantity, pricePerItem, totalPrice);
    }

    @Override
    public @NotNull String toString() {
        return "SellGUIItemSellEvent{" + "player=" + getPlayer().getName() + ", material=" + material +
                ", quantity=" + quantity + ", pricePerItem=" + pricePerItem + ", totalPrice=" + totalPrice + '}';
    }
}