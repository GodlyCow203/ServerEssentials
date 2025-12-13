package com.serveressentials.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface VaultAPI {

    /**
     * Maximum number of vaults per player (10).
     */
    int MAX_VAULTS = 10;

    /**
     * Open a vault for a player.
     * @param player The player to open vault for
     * @param vaultNumber Vault number (1-10)
     * @return true if vault was opened, false if player lacks permission
     * @throws IllegalArgumentException if vaultNumber is invalid
     */
    boolean openVault(Player player, int vaultNumber);

    /**
     * Get a vault's contents as inventory without opening it.
     * @param playerUUID Player's UUID
     * @param vaultNumber Vault number (1-10)
     * @return CompletableFuture with the vault inventory
     */
    CompletableFuture<Inventory> getVaultInventory(UUID playerUUID, int vaultNumber);

    /**
     * Save items to a player's vault (admin function).
     * @param playerUUID Player's UUID
     * @param vaultNumber Vault number (1-10)
     * @param inventory Inventory to save
     * @return CompletableFuture that completes when save is done
     */
    CompletableFuture<Void> saveVault(UUID playerUUID, int vaultNumber, Inventory inventory);

    /**
     * Check if a vault exists and has items.
     * @param playerUUID Player's UUID
     * @param vaultNumber Vault number (1-10)
     * @return CompletableFuture with true if vault has data
     */
    CompletableFuture<Boolean> hasVault(UUID playerUUID, int vaultNumber);

    /**
     * Clear a vault's contents.
     * @param playerUUID Player's UUID
     * @param vaultNumber Vault number (1-10)
     * @return CompletableFuture that completes when cleared
     */
    CompletableFuture<Void> clearVault(UUID playerUUID, int vaultNumber);
}