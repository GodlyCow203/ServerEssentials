package com.serveressentials.api.shop;

import org.bukkit.entity.Player;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;


public interface ShopAPI {
    /**
     * Opens the main shop GUI for a player
     * @param player The player to open the shop for
     */
    void openShop(Player player);

    /**
     * Opens a specific shop section for a player
     * @param player The player to open the section for
     * @param sectionName The name of the section (without .yml extension)
     */
    void openShopSection(Player player, String sectionName);

    /**
     * Reloads all shop configurations from files/database
     * @return Future that completes with true if successful
     */
    CompletableFuture<Boolean> reloadShop();

    /**
     * Checks if the shop system is enabled
     */
    boolean isShopEnabled();

    /**
     * Gets a shop section configuration by name
     * @param sectionName The section name
     * @return The shop section, or null if not found
     */
    ShopSection getSection(String sectionName);

    /**
     * Gets all available shop section names
     * @return Collection of section names
     */
    Collection<String> getSectionNames();
}