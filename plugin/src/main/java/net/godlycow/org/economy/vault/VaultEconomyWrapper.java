package net.godlycow.org.economy.vault;

import net.godlycow.org.economy.eco.InternalEconomy;
import net.godlycow.org.util.logger.AnsiColorUtil;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.UUID;

public class VaultEconomyWrapper implements Economy {
    private final InternalEconomy economy;
    private final String name;

    public VaultEconomyWrapper(InternalEconomy economy, String name) {
        this.economy = economy;
        this.name = name;
    }


    @Override
    public boolean isEnabled() {
        return economy.isEnabled();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String format(double amount) {
        return economy.format(amount);
    }


    @Override
    public boolean hasAccount(OfflinePlayer player) {
        try {
            return economy.hasAccount(player);
        } catch (Exception e) {
            Bukkit.getLogger().warning(AnsiColorUtil.danger(
                    "VaultEconomyWrapper: Error checking account for " + player.getName() + ": " + e.getMessage()
            ));
            return false;
        }
    }

    @Override
    public boolean hasAccount(String identifier) {
        return hasAccount(resolvePlayer(identifier));
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player);
    }

    @Override
    public boolean hasAccount(String identifier, String worldName) {
        return hasAccount(resolvePlayer(identifier));
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        try {
            return economy.createPlayerAccount(player);
        } catch (Exception e) {
            Bukkit.getLogger().warning(AnsiColorUtil.danger(
                    "VaultEconomyWrapper: Error creating account for " + player.getName() + ": " + e.getMessage()
            ));
            return false;
        }
    }

    @Override
    public boolean createPlayerAccount(String identifier) {
        return createPlayerAccount(resolvePlayer(identifier));
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return createPlayerAccount(player);
    }

    @Override
    public boolean createPlayerAccount(String identifier, String worldName) {
        return createPlayerAccount(resolvePlayer(identifier));
    }


    @Override
    public double getBalance(OfflinePlayer player) {
        try {
            return economy.getBalance(player);
        } catch (Exception e) {
            Bukkit.getLogger().warning(AnsiColorUtil.danger(
                    "VaultEconomyWrapper: Error getting balance for " + player.getName() + ": " + e.getMessage()
            ));
            return 0.0;
        }
    }

    @Override
    public double getBalance(String identifier) {
        return getBalance(resolvePlayer(identifier));
    }

    @Override
    public double getBalance(OfflinePlayer player, String worldName) {
        return getBalance(player);
    }

    @Override
    public double getBalance(String identifier, String world) {
        return getBalance(resolvePlayer(identifier));
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        try {
            return economy.has(player, amount);
        } catch (Exception e) {
            Bukkit.getLogger().warning(AnsiColorUtil.danger(
                    "VaultEconomyWrapper: Error checking funds for " + player.getName() + ": " + e.getMessage()
            ));
            return false;
        }
    }

    @Override
    public boolean has(String identifier, double amount) {
        return has(resolvePlayer(identifier), amount);
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }

    @Override
    public boolean has(String identifier, String worldName, double amount) {
        return has(resolvePlayer(identifier), amount);
    }


    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        try {
            net.godlycow.org.economy.eco.EconomyResponse internal = economy.withdrawPlayer(player, amount);
            return convertResponse(internal);
        } catch (Exception e) {
            return failureResponse(amount, 0, "Withdraw error: " + e.getMessage());
        }
    }

    @Override
    public EconomyResponse withdrawPlayer(String identifier, double amount) {
        return withdrawPlayer(resolvePlayer(identifier), amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String identifier, String worldName, double amount) {
        return withdrawPlayer(resolvePlayer(identifier), amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        try {
            net.godlycow.org.economy.eco.EconomyResponse internal = economy.depositPlayer(player, amount);
            return convertResponse(internal);
        } catch (Exception e) {
            return failureResponse(amount, 0, "Deposit error: " + e.getMessage());
        }
    }

    @Override
    public EconomyResponse depositPlayer(String identifier, double amount) {
        return depositPlayer(resolvePlayer(identifier), amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String identifier, String worldName, double amount) {
        return depositPlayer(resolvePlayer(identifier), amount);
    }


    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return notImplemented();
    }

    @Override
    public EconomyResponse createBank(String name, String player) {
        return notImplemented();
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return notImplemented();
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return notImplemented();
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return notImplemented();
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return notImplemented();
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return notImplemented();
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return notImplemented();
    }

    @Override
    public EconomyResponse isBankOwner(String name, String player) {
        return notImplemented();
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return notImplemented();
    }

    @Override
    public EconomyResponse isBankMember(String name, String player) {
        return notImplemented();
    }

    @Override
    public List<String> getBanks() {
        return List.of();
    }


    @Override
    public int fractionalDigits() {
        return 2;
    }

    @Override
    public String currencyNamePlural() {
        return "";
    }

    @Override
    public String currencyNameSingular() {
        return "";
    }


    private OfflinePlayer resolvePlayer(String identifier) {
        try {
            UUID uuid = UUID.fromString(identifier);
            return Bukkit.getOfflinePlayer(uuid);
        } catch (IllegalArgumentException e) {
            return Bukkit.getOfflinePlayer(identifier);
        }
    }

    private EconomyResponse convertResponse(net.godlycow.org.economy.eco.EconomyResponse internal) {
        EconomyResponse.ResponseType vaultType = switch (internal.type) {
            case SUCCESS -> EconomyResponse.ResponseType.SUCCESS;
            case FAILURE -> EconomyResponse.ResponseType.FAILURE;
            case NOT_IMPLEMENTED -> EconomyResponse.ResponseType.NOT_IMPLEMENTED;
        };
        return new EconomyResponse(internal.amount, internal.balance, vaultType, internal.errorMessage);
    }

    private EconomyResponse notImplemented() {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banking not supported");
    }

    private EconomyResponse failureResponse(double amount, double balance, String error) {
        return new EconomyResponse(amount, balance, EconomyResponse.ResponseType.FAILURE, error);
    }
}