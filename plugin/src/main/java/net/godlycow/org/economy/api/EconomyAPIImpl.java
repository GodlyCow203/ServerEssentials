package net.godlycow.org.economy.api;

import com.serveressentials.api.economy.EconomyAPI;
import com.serveressentials.api.economy.EconomyPaymentSettings;
import com.serveressentials.api.economy.EconomyResponse;
import com.serveressentials.api.economy.event.EconomyDepositEvent;
import com.serveressentials.api.economy.event.EconomyPaymentSettingsChangeEvent;
import com.serveressentials.api.economy.event.EconomyWithdrawEvent;
import net.godlycow.org.ServerEssentials;
import net.godlycow.org.economy.eco.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public final class EconomyAPIImpl implements EconomyAPI {
    private final @NotNull ServerEssentials plugin;
    private final @NotNull EconomyManager economyManager;

    public EconomyAPIImpl(@NotNull ServerEssentials plugin, @NotNull EconomyManager economyManager) {
        this.plugin = plugin;
        this.economyManager = economyManager;
    }

    @Override
    public boolean isEnabled() {
        return economyManager.isEnabled();
    }

    @Override
    public @NotNull String getEconomyName() {
        return economyManager.getEconomyName();
    }

    @Override
    public @NotNull CompletableFuture<Double> getBalance(@NotNull OfflinePlayer player) {
        return CompletableFuture.supplyAsync(() -> economyManager.getBalance(player.getPlayer()));
    }

    @Override
    public @NotNull CompletableFuture<Boolean> has(@NotNull OfflinePlayer player, double amount) {
        return CompletableFuture.supplyAsync(() -> economyManager.has(player.getPlayer(), amount));
    }

    @Override
    public @NotNull CompletableFuture<EconomyResponse> deposit(@NotNull OfflinePlayer player, double amount) {
        return CompletableFuture.supplyAsync(() -> {
            net.godlycow.org.economy.eco.EconomyResponse internal = economyManager.deposit(player.getPlayer(), amount);
            EconomyResponse response = new EconomyResponse(
                    internal.amount, internal.balance, internal.success(), internal.errorMessage
            );

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                Bukkit.getPluginManager().callEvent(new EconomyDepositEvent(player, amount, response));
            });

            return response;
        });
    }

    @Override
    public @NotNull CompletableFuture<EconomyResponse> withdraw(@NotNull OfflinePlayer player, double amount) {
        return CompletableFuture.supplyAsync(() -> {
            net.godlycow.org.economy.eco.EconomyResponse internal = economyManager.withdraw(player.getPlayer(), amount);
            EconomyResponse response = new EconomyResponse(
                    internal.amount, internal.balance, internal.success(), internal.errorMessage
            );

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                Bukkit.getPluginManager().callEvent(new EconomyWithdrawEvent(player, amount, response));
            });

            return response;
        });
    }

    @Override
    public @NotNull String format(double amount) {
        return economyManager.format(amount);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> createAccount(@NotNull OfflinePlayer player) {
        return CompletableFuture.supplyAsync(() -> economyManager.getEconomy().createPlayerAccount(player));
    }

    @Override
    public @NotNull CompletableFuture<EconomyPaymentSettings> getPaymentSettings(@NotNull String playerUuid) {
        return economyManager.hasPaymentsDisabled(playerUuid).thenCombine(
                economyManager.hasPayConfirmDisabled(playerUuid),
                (paymentsDisabled, confirmDisabled) ->
                        new EconomyPaymentSettings(paymentsDisabled, confirmDisabled)
        );
    }

    @Override
    public @NotNull CompletableFuture<Void> setPaymentsDisabled(@NotNull String playerUuid,
                                                                @NotNull String playerName,
                                                                boolean disabled) {
        return getPaymentSettings(playerUuid).thenCompose(oldSettings -> {
            return economyManager.setPaymentsDisabled(playerUuid, playerName, disabled).thenRun(() -> {
                EconomyPaymentSettings newSettings = new EconomyPaymentSettings(disabled, oldSettings.isPayConfirmDisabled());

                OfflinePlayer player = Bukkit.getOfflinePlayer(java.util.UUID.fromString(playerUuid));
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    Bukkit.getPluginManager().callEvent(
                            new EconomyPaymentSettingsChangeEvent(player, oldSettings, newSettings)
                    );
                });
            });
        });
    }

    @Override
    public @NotNull CompletableFuture<Void> setPayConfirmDisabled(@NotNull String playerUuid,
                                                                  @NotNull String playerName,
                                                                  boolean disabled) {
        return getPaymentSettings(playerUuid).thenCompose(oldSettings -> {
            return economyManager.setPayConfirmDisabled(playerUuid, playerName, disabled).thenRun(() -> {
                EconomyPaymentSettings newSettings = new EconomyPaymentSettings(oldSettings.isPaymentsDisabled(), disabled);

                OfflinePlayer player = Bukkit.getOfflinePlayer(java.util.UUID.fromString(playerUuid));
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    Bukkit.getPluginManager().callEvent(
                            new EconomyPaymentSettingsChangeEvent(player, oldSettings, newSettings)
                    );
                });
            });
        });
    }

    @Override
    public @NotNull CompletableFuture<Void> reload() {
        return CompletableFuture.runAsync(() -> {
            plugin.getLogger().info("[ServerEssentials] Economy configuration reloaded");
        });
    }
}