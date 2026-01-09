package com.serveressentials.api.auction;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


public interface AuctionAPI {
    /**
     * Opens the main auction GUI for a player
     * @param player The player to open the auction for
     */
    void openAuction(Player player);

    /**
     * Opens a player's listed items GUI
     * @param player The player to view their items
     */
    void openMyAuctionItems(Player player);

    /**
     * Adds a new item to the auction
     * @param seller The player selling the item
     * @param item The item stack to auction
     * @param price The selling price
     * @return Future that completes with true if successful
     */
    CompletableFuture<Boolean> addAuctionItem(Player seller, ItemStack item, double price);

    /**
     * Removes an item from auction (returns it to seller)
     * @param itemId The UUID of the auction item
     * @return Future that completes with true if successful
     */
    CompletableFuture<Boolean> removeAuctionItem(UUID itemId);

    /**
     * Gets all currently active auction items
     *
     * @return Future with collection of auction items
     */
    CompletableFuture<@NotNull List<AuctionItem>> getActiveItems();

    /**
     * Gets all auction items for a specific player
     *
     * @param playerId The player's UUID
     * @return Future with collection of player's auction items
     */
    CompletableFuture<@NotNull List<AuctionItem>> getPlayerItems(UUID playerId);

    /**
     * Checks if the auction system is enabled
     * @return true if enabled
     */
    boolean isAuctionEnabled();

    /**
     * Gets the maximum price limit for auction items
     * @return The maximum price allowed
     */
    double getMaxPriceLimit();

    /**
     * Gets the maximum number of items a player can list
     * @return The maximum items per player
     */
    int getMaxItemsPerPlayer();
}