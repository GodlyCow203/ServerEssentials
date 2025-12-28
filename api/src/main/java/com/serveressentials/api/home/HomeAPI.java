package com.serveressentials.api.home;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * API interface for the ServerEssentials Home System
 */
public interface HomeAPI {
    /**
     * Sets a home for a player at a specific slot
     * @param player The player to set the home for
     * @param slot The slot number (1-based indexing)
     * @param name The name of the home
     * @param location The location of the home
     * @return Future that completes with true if successful
     */
    CompletableFuture<Boolean> setHome(Player player, int slot, String name, Location location);

    /**
     * Gets a home by slot for a player
     * @param player The player to get the home for
     * @param slot The slot number
     * @return Future that completes with the home if found
     */
    CompletableFuture<Optional<Home>> getHome(Player player, int slot);

    /**
     * Gets all homes for a player
     * @param player The player to get homes for
     * @return Future that completes with a map of slot -> home
     */
    CompletableFuture<Map<Integer, Home>> getAllHomes(Player player);

    /**
     * Removes a home by slot for a player
     * @param player The player to remove the home for
     * @param slot The slot number to remove
     * @return Future that completes with true if successful
     */
    CompletableFuture<Boolean> removeHome(Player player, int slot);

    /**
     * Gets the maximum number of homes a player can have
     * @param player The player to check
     * @return The maximum number of homes allowed
     */
    int getMaxHomes(Player player);

    /**
     * Gets the number of homes a player currently has
     * @param player The player to check
     * @return The current number of homes
     */
    CompletableFuture<Integer> getHomeCount(Player player);
}