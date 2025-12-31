package com.serveressentials.api.sellgui;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;
import java.util.UUID;

public final class SellTransaction {
    private final @NotNull UUID playerId;
    private final @NotNull String playerName;
    private final @NotNull Material material;
    private final int quantity;
    private final double pricePerItem;
    private final double totalPrice;
    private final long timestamp;

    public SellTransaction(@NotNull UUID playerId, @NotNull String playerName, @NotNull Material material,
                           int quantity, double pricePerItem, double totalPrice, long timestamp) {
        this.playerId = Objects.requireNonNull(playerId, "playerId cannot be null");
        this.playerName = Objects.requireNonNull(playerName, "playerName cannot be null");
        this.material = Objects.requireNonNull(material, "material cannot be null");
        this.quantity = quantity;
        this.pricePerItem = pricePerItem;
        this.totalPrice = totalPrice;
        this.timestamp = timestamp;
    }

    public @NotNull UUID getPlayerId() {
        return playerId;
    }

    public @NotNull String getPlayerName() {
        return playerName;
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

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SellTransaction that = (SellTransaction) o;
        return quantity == that.quantity && Double.compare(that.pricePerItem, pricePerItem) == 0 &&
                Double.compare(that.totalPrice, totalPrice) == 0 && timestamp == that.timestamp &&
                Objects.equals(playerId, that.playerId) && Objects.equals(playerName, that.playerName) &&
                material == that.material;
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, playerName, material, quantity, pricePerItem, totalPrice, timestamp);
    }

    @Override
    public @NotNull String toString() {
        return "SellTransaction{" + "playerId=" + playerId + ", playerName='" + playerName + '\'' +
                ", material=" + material + ", quantity=" + quantity + ", pricePerItem=" + pricePerItem +
                ", totalPrice=" + totalPrice + ", timestamp=" + timestamp + '}';
    }
}