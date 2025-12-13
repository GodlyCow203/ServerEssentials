package com.serveressentials.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Public API for player vault operations.
 * All methods are async-safe and return CompletableFuture for non-blocking operations.
 */
public interface VaultAPI {

    /** Maximum number of vaults per player */
    int MAX_VAULTS = 10;

    /**
     * Opens a vault for a player with permission check.
     * @param player The player (must not be null)
     * @param vaultNumber Vault number (1-10)
     * @return true if vault was opened, false if player lacks permission or error occurred
     * @throws IllegalArgumentException if vaultNumber is out of range
     * @throws NullPointerException if player is null
     */
    boolean openVault(@NotNull Player player, int vaultNumber);

    /**
     * Gets a vault's contents asynchronously without opening it.
     * @param playerUUID Player's UUID (must not be null)
     * @param vaultNumber Vault number (1-10)
     * @return CompletableFuture with inventory snapshot (never null, empty vault if none exists)
     * @throws IllegalArgumentException if vaultNumber is out of range
     * @throws NullPointerException if playerUUID is null
     */
    @NotNull
    CompletableFuture<Inventory> getVaultInventory(@NotNull UUID playerUUID, int vaultNumber);

    /**
     * Saves an inventory to a player's vault (admin function).
     * @param playerUUID Player's UUID (must not be null)
     * @param vaultNumber Vault number (1-10)
     * @param inventory Inventory to save (must not be null)
     * @return CompletableFuture that completes when save is done
     * @throws IllegalArgumentException if vaultNumber is out of range
     * @throws NullPointerException if any parameter is null
     */
    @NotNull
    CompletableFuture<Void> saveVault(@NotNull UUID playerUUID, int vaultNumber, @NotNull Inventory inventory);

    /**
     * Checks if a vault exists and contains items.
     * @param playerUUID Player's UUID (must not be null)
     * @param vaultNumber Vault number (1-10)
     * @return CompletableFuture with true if vault has saved data
     * @throws IllegalArgumentException if vaultNumber is out of range
     * @throws NullPointerException if playerUUID is null
     */
    @NotNull
    CompletableFuture<Boolean> hasVault(@NotNull UUID playerUUID, int vaultNumber);

    /**
     * Clears a vault's contents permanently.
     * @param playerUUID Player's UUID (must not be null)
     * @param vaultNumber Vault number (1-10)
     * @return CompletableFuture that completes when cleared
     * @throws IllegalArgumentException if vaultNumber is out of range
     * @throws NullPointerException if playerUUID is null
     */
    @NotNull
    CompletableFuture<Void> clearVault(@NotNull UUID playerUUID, int vaultNumber);

    /**
     * Gets the count of non-empty vaults a player has.
     * @param playerUUID Player's UUID (must not be null)
     * @return CompletableFuture with count (0-10)
     * @throws NullPointerException if playerUUID is null
     */
    @NotNull
    CompletableFuture<Integer> getVaultCount(@NotNull UUID playerUUID);

    /**
     * Validates a vault number.
     * @param number The number to check
     * @return true if 1-10, false otherwise
     */
    static boolean isValidVaultNumber(int number) {
        return number >= 1 && number <= MAX_VAULTS;
    }
}