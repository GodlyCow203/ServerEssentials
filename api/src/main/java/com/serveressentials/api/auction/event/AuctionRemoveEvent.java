package com.serveressentials.api.auction.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import java.util.UUID;


public class AuctionRemoveEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final ItemStack item;
    private final UUID itemId;

    public AuctionRemoveEvent(Player player, ItemStack item, UUID itemId) {
        this.player = player;
        this.item = item;
        this.itemId = itemId;
    }

    public Player getPlayer() { return player; }
    public ItemStack getItem() { return item; }
    public UUID getItemId() { return itemId; }

    @Override
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}