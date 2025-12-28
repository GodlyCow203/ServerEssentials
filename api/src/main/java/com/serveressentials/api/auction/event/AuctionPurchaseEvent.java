package com.serveressentials.api.auction.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import java.util.UUID;


public class AuctionPurchaseEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player buyer;
    private final UUID sellerId;
    private final ItemStack item;
    private final double price;

    public AuctionPurchaseEvent(Player buyer, UUID sellerId, ItemStack item, double price) {
        this.buyer = buyer;
        this.sellerId = sellerId;
        this.item = item;
        this.price = price;
    }

    public Player getBuyer() { return buyer; }
    public UUID getSellerId() { return sellerId; }
    public ItemStack getItem() { return item; }
    public double getPrice() { return price; }

    @Override
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}