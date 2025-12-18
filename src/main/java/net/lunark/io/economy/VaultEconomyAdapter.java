package net.lunark.io.economy;

import net.lunark.io.hooks.VaultHook;
import org.bukkit.OfflinePlayer;
import java.util.concurrent.CompletableFuture;


public class VaultEconomyAdapter implements EconomyAPI {
    private final VaultHook vaultHook;

    public VaultEconomyAdapter(VaultHook vaultHook) {
        this.vaultHook = vaultHook;
    }

    @Override
    public boolean isEnabled() {
        return vaultHook.isAvailable() && vaultHook.hasEconomy();
    }

    @Override
    public String getName() {
        return vaultHook.getEconomy().getName();
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return vaultHook.getEconomy().getBalance(player);
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return vaultHook.getEconomy().has(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        net.milkbowl.vault.economy.EconomyResponse vaultResp =
                vaultHook.getEconomy().depositPlayer(player, amount);
        return new EconomyResponse(
                vaultResp.amount,
                vaultResp.balance,
                vaultResp.type == net.milkbowl.vault.economy.EconomyResponse.ResponseType.SUCCESS ?
                        EconomyResponse.ResponseType.SUCCESS : EconomyResponse.ResponseType.FAILURE,
                vaultResp.errorMessage
        );
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        net.milkbowl.vault.economy.EconomyResponse vaultResp =
                vaultHook.getEconomy().withdrawPlayer(player, amount);
        return new EconomyResponse(
                vaultResp.amount,
                vaultResp.balance,
                vaultResp.type == net.milkbowl.vault.economy.EconomyResponse.ResponseType.SUCCESS ?
                        EconomyResponse.ResponseType.SUCCESS : EconomyResponse.ResponseType.FAILURE,
                vaultResp.errorMessage
        );
    }

    @Override
    public String format(double amount) {
        return vaultHook.getEconomy().format(amount);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return vaultHook.getEconomy().createPlayerAccount(player);
    }

    @Override
    public CompletableFuture<Boolean> hasPaymentsDisabled(String playerUuid) {
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public CompletableFuture<Void> setPaymentsDisabled(String playerUuid, String playerName, boolean disabled) {
        return CompletableFuture.completedFuture(null);
    }
}