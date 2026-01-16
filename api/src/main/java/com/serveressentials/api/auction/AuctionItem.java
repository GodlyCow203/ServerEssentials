package com.serveressentials.api.auction;

import org.bukkit.inventory.ItemStack;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


public class AuctionItem {
    private final UUID id;
    private final UUID seller;
    private final ItemStack item;
    private final double price;
    private final long expiration;
    private final long createdAt;

    public AuctionItem(UUID id, UUID seller, ItemStack item, double price, long expiration, long createdAt) {
        this.id = id;
        this.seller = seller;
        this.item = item;
        this.price = price;
        this.expiration = expiration;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getSeller() { return seller; }
    public ItemStack getItem() { return item; }
    public double getPrice() { return price; }
    public long getExpiration() { return expiration; }
    public long getCreatedAt() { return createdAt; }
}