package net.godlycow.org.economy.eco;

import org.bukkit.OfflinePlayer;
import java.util.concurrent.CompletableFuture;


public interface EconomyAPI {
    boolean isEnabled();
    String getName();
    double getBalance(OfflinePlayer player);
    boolean has(OfflinePlayer player, double amount);
    EconomyResponse depositPlayer(OfflinePlayer player, double amount);
    EconomyResponse withdrawPlayer(OfflinePlayer player, double amount);
    String format(double amount);
    boolean createPlayerAccount(OfflinePlayer player);

    CompletableFuture<Boolean> hasPaymentsDisabled(String playerUuid);
    CompletableFuture<Void> setPaymentsDisabled(String playerUuid, String playerName, boolean disabled);
    default CompletableFuture<Boolean> hasPayConfirmDisabled(String playerUuid) {
        return CompletableFuture.completedFuture(false);
    }

    default CompletableFuture<Void> setPayConfirmDisabled(String playerUuid, String playerName, boolean disabled) {
        return CompletableFuture.completedFuture(null);
    }
}