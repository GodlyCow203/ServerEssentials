package com.serveressentials.api.sellgui;

import com.serveressentials.api.sellgui.event.SellGUITransactionCompleteEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SellGUIAPI {
    /**
     * Opens the sell GUI for a player
     * @param player The player to open GUI for
     * @return CompletableFuture with success status
     */
    @NotNull CompletableFuture<Boolean> openSellGUI(@NotNull Player player);

    /**
     * Checks if a material is sellable
     * @param material The material to check
     * @return true if sellable
     */
    boolean isSellable(@NotNull Material material);

    /**
     * Gets the sell price for a material
     * @param material The material to price
     * @return The price per item, or 0 if not sellable
     */
    double getSellPrice(@NotNull Material material);

    /**
     * Gets all sellable items
     * @return CompletableFuture with list of sellable items
     */
    @NotNull CompletableFuture<List<SellableItem>> getSellableItems();

    /**
     * Gets the GUI layout configuration
     * @return The layout data
     */
    @NotNull SellGUILayout getLayout();

    /**
     * Processes a sell transaction (for programmatic selling)
     * @param player The player selling items
     * @param items The items to sell (material -> quantity map)
     * @return CompletableFuture with transaction result
     */
    @NotNull CompletableFuture<SellTransaction> processSellTransaction(@NotNull Player player, @NotNull java.util.Map<Material, Integer> items);

    /**
     * Logs a sell transaction to storage
     * @param transaction The transaction to log
     * @return CompletableFuture when logged
     */
    @NotNull CompletableFuture<Void> logTransaction(@NotNull SellTransaction transaction);

    /**
     * Checks if the sell GUI system is enabled
     * @return true if enabled
     */
    boolean isEnabled();

    /**
     * Gets the currency symbol
     * @return The currency symbol
     */
    @NotNull String getCurrencySymbol();

    /**
     * Reloads the sell GUI configuration
     * @return CompletableFuture when reload completes
     */
    @NotNull CompletableFuture<Void> reload();
}