package com.serveressentials.api.economy;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * API for managing player economy and transactions
 */
public interface EconomyAPI {

    /**
     * Checks if the economy system is enabled
     * @return true if enabled
     */
    boolean isEnabled();

    /**
     * Gets the economy system name (e.g., "Vault", "Internal")
     * @return The economy name
     */
    @NotNull String getEconomyName();

    /**
     * Gets a player's current balance
     * @param player The offline player
     * @return CompletableFuture with the player's balance
     */
    @NotNull CompletableFuture<Double> getBalance(@NotNull OfflinePlayer player);

    /**
     * Checks if a player has at least the specified amount
     * @param player The offline player
     * @param amount The amount to check
     * @return CompletableFuture with true if player has enough
     */
    @NotNull CompletableFuture<Boolean> has(@NotNull OfflinePlayer player, double amount);

    /**
     * Deposits money into a player's account
     * @param player The offline player
     * @param amount The amount to deposit
     * @return CompletableFuture with the transaction response
     */
    @NotNull CompletableFuture<EconomyResponse> deposit(@NotNull OfflinePlayer player, double amount);

    /**
     * Withdraws money from a player's account
     * @param player The offline player
     * @param amount The amount to withdraw
     * @return CompletableFuture with the transaction response
     */
    @NotNull CompletableFuture<EconomyResponse> withdraw(@NotNull OfflinePlayer player, double amount);

    /**
     * Formats an amount as a currency string
     * @param amount The amount to format
     * @return The formatted string (e.g., "$100.00")
     */
    @NotNull String format(double amount);

    /**
     * Creates a player account if it doesn't exist
     * @param player The offline player
     * @return CompletableFuture with true if account was created
     */
    @NotNull CompletableFuture<Boolean> createAccount(@NotNull OfflinePlayer player);

    /**
     * Gets a player's payment settings
     * @param playerUuid The player's UUID as string
     * @return CompletableFuture with payment settings DTO
     */
    @NotNull CompletableFuture<EconomyPaymentSettings> getPaymentSettings(@NotNull String playerUuid);

    /**
     * Sets a player's payment disabled status
     * @param playerUuid The player's UUID as string
     * @param playerName The player's name
     * @param disabled true to disable payments
     * @return CompletableFuture that completes when set
     */
    @NotNull CompletableFuture<Void> setPaymentsDisabled(@NotNull String playerUuid,
                                                         @NotNull String playerName,
                                                         boolean disabled);

    /**
     * Sets a player's pay confirm disabled status
     * @param playerUuid The player's UUID as string
     * @param playerName The player's name
     * @param disabled true to disable confirmation
     * @return CompletableFuture that completes when set
     */
    @NotNull CompletableFuture<Void> setPayConfirmDisabled(@NotNull String playerUuid,
                                                           @NotNull String playerName,
                                                           boolean disabled);

    /**
     * Reloads economy configuration
     * @return CompletableFuture that completes when reloaded
     */
    @NotNull CompletableFuture<Void> reload();
}