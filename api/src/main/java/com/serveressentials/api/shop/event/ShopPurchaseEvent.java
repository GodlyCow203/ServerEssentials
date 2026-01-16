package com.serveressentials.api.shop.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * Event fired when a player purchases an item from the shop
 */
public class ShopPurchaseEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final ItemStack item;
    private final double price;
    private final int amount;

    public ShopPurchaseEvent(Player player, ItemStack item, double price, int amount) {
        this.player = player;
        this.item = item;
        this.price = price;
        this.amount = amount;
    }

    public Player getPlayer() { return player; }
    public ItemStack getItem() { return item; }
    public double getPrice() { return price; }
    public int getAmount() { return amount; }

    @Override
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}