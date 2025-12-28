package com.serveressentials.api.auction.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;


public class AuctionListEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final ItemStack item;
    private final double price;

    public AuctionListEvent(Player player, ItemStack item, double price) {
        this.player = player;
        this.item = item;
        this.price = price;
    }

    public Player getPlayer() { return player; }
    public ItemStack getItem() { return item; }
    public double getPrice() { return price; }

    @Override
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}


