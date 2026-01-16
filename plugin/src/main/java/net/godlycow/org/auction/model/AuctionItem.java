package net.godlycow.org.auction.model;

import org.bukkit.inventory.ItemStack;
import java.util.UUID;
import java.util.Objects;

public class AuctionItem {
    private final UUID id;
    private final UUID seller;
    private final ItemStack item;
    private final double price;
    private final long expiration;

    public AuctionItem(UUID seller, ItemStack item, double price, long expiration) {
        this(UUID.randomUUID(), seller, item, price, expiration);
    }

    public AuctionItem(UUID id, UUID seller, ItemStack item, double price, long expiration) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.seller = Objects.requireNonNull(seller, "Seller cannot be null");
        this.item = Objects.requireNonNull(item, "Item cannot be null");
        if (item.getType().isAir()) {
            throw new IllegalArgumentException("Item cannot be AIR");
        }
        this.price = price;
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        this.expiration = expiration;
    }

    public UUID getId() { return id; }
    public UUID getSeller() { return seller; }
    public ItemStack getItem() { return item.clone(); }
    public double getPrice() { return price; }
    public long getExpiration() { return expiration; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuctionItem that = (AuctionItem) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}