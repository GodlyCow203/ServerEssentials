package net.lunark.io.economy;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.lunark.io.database.DatabaseManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class ServerEssentialsEconomy implements Economy {
    private final Plugin plugin;
    private final DatabaseManager databaseManager;
    private final String poolKey = "economy";

    public ServerEssentialsEconomy(Plugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        initializeDatabase();
    }

    private void initializeDatabase() {
        String balancesSql = "CREATE TABLE IF NOT EXISTS economy_balances (" +
                "player_uuid TEXT PRIMARY KEY, " +
                "player_name TEXT NOT NULL, " +
                "balance REAL NOT NULL DEFAULT 0.0)";

        String settingsSql = "CREATE TABLE IF NOT EXISTS economy_settings (" +
                "player_uuid TEXT PRIMARY KEY, " +
                "player_name TEXT NOT NULL, " +
                "payments_disabled BOOLEAN NOT NULL DEFAULT FALSE, " +
                "pay_confirm_disabled BOOLEAN NOT NULL DEFAULT FALSE)";


        CompletableFuture.allOf(
                databaseManager.executeUpdate(poolKey, balancesSql),
                databaseManager.executeUpdate(poolKey, settingsSql)
        ).join();

        plugin.getLogger().info("[Economy] Tables initialized");
    }

    public CompletableFuture<Boolean> hasPayConfirmDisabled(UUID playerUuid) {
        String sql = "SELECT pay_confirm_disabled FROM economy_settings WHERE player_uuid = ?";
        return databaseManager.executeQuery(poolKey, sql,
                rs -> rs.next() ? rs.getBoolean("pay_confirm_disabled") : false,
                playerUuid.toString()).thenApply(opt -> opt.orElse(false));
    }

    public CompletableFuture<Void> setPayConfirmDisabled(UUID playerUuid, String playerName, boolean disabled) {
        String sql = "INSERT OR REPLACE INTO economy_settings (player_uuid, player_name, payments_disabled, pay_confirm_disabled) " +
                "VALUES (?, ?, " +
                "COALESCE((SELECT payments_disabled FROM economy_settings WHERE player_uuid = ?), FALSE), " +
                "?)";
        return databaseManager.executeUpdate(poolKey, sql, playerUuid.toString(), playerName, playerUuid.toString(), disabled);
    }

    public CompletableFuture<Boolean> hasPaymentsDisabled(UUID playerUuid) {
        String sql = "SELECT payments_disabled FROM economy_settings WHERE player_uuid = ?";
        return databaseManager.executeQuery(poolKey, sql,
                rs -> rs.next() ? rs.getBoolean("payments_disabled") : false,
                playerUuid.toString()).thenApply(opt -> opt.orElse(false));
    }

    public CompletableFuture<Void> setPaymentsDisabled(UUID playerUuid, String playerName, boolean disabled) {
        String sql = "INSERT OR REPLACE INTO economy_settings (player_uuid, player_name, payments_disabled) VALUES (?, ?, ?)";
        return databaseManager.executeUpdate(poolKey, sql, playerUuid.toString(), playerName, disabled);
    }

    private CompletableFuture<Double> getBalanceAsync(UUID playerUuid) {
        String sql = "SELECT balance FROM economy_balances WHERE player_uuid = ?";
        return databaseManager.executeQuery(poolKey, sql,
                        rs -> rs.next() ? rs.getDouble("balance") : 0.0,
                        playerUuid.toString())
                .thenApply(opt -> opt.orElse(0.0))
                .exceptionally(ex -> {
                    plugin.getLogger().log(Level.SEVERE, "[Economy] Failed to get balance for " + playerUuid, ex);
                    return 0.0;
                });
    }

    private CompletableFuture<Void> setBalanceAsync(UUID playerUuid, String playerName, double amount) {
        String sql = "INSERT OR REPLACE INTO economy_balances (player_uuid, player_name, balance) VALUES (?, ?, ?)";
        return databaseManager.executeUpdate(poolKey, sql, playerUuid.toString(), playerName, Math.max(0, amount));
    }

    @Override public boolean isEnabled() { return true; }
    @Override public String getName() { return "ServerEssentials"; }
    @Override public boolean hasBankSupport() { return false; }
    @Override public int fractionalDigits() { return 2; }
    @Override public String format(double amount) { return String.format("$%.2f", amount); }
    @Override public String currencyNamePlural() { return "Dollars"; }
    @Override public String currencyNameSingular() { return "Dollar"; }

    @Override public boolean hasAccount(String s) { return false; }
    @Override public boolean hasAccount(OfflinePlayer player) { return true; }
    @Override public boolean hasAccount(String s, String s1) { return false; }
    @Override public boolean hasAccount(OfflinePlayer player, String worldName) { return hasAccount(player); }

    @Override public double getBalance(String s) { return 0; }

    @Override
    public double getBalance(OfflinePlayer player) {
        try {
            return getBalanceAsync(player.getUniqueId()).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            plugin.getLogger().warning("[Economy] Timeout getting balance for " + player.getName());
            return 0.0;
        }
    }

    @Override public double getBalance(String s, String s1) { return 0; }
    @Override public double getBalance(OfflinePlayer player, String world) { return getBalance(player); }

    @Override public boolean has(String s, double v) { return false; }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        try {
            return getBalanceAsync(player.getUniqueId()).get(5, TimeUnit.SECONDS) >= amount;
        } catch (Exception e) {
            plugin.getLogger().warning("[Economy] Timeout checking balance for " + player.getName());
            return false;
        }
    }

    @Override public boolean has(String s, String s1, double v) { return false; }
    @Override public boolean has(OfflinePlayer player, String worldName, double amount) { return has(player, amount); }

    @Override public EconomyResponse withdrawPlayer(String s, double v) { return null; }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative funds");
        }

        try {
            UUID uuid = player.getUniqueId();
            double currentBalance = getBalanceAsync(uuid).get(5, TimeUnit.SECONDS);

            if (currentBalance < amount) {
                return new EconomyResponse(0, currentBalance, EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
            }

            double newBalance = currentBalance - amount;
            setBalanceAsync(uuid, player.getName(), newBalance).get(5, TimeUnit.SECONDS);

            return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, null);
        } catch (Exception e) {
            plugin.getLogger().severe("[Economy] Withdraw failed for " + player.getName() + ": " + e.getMessage());
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Database error");
        }
    }

    @Override public EconomyResponse withdrawPlayer(String s, String s1, double v) { return null; }
    @Override public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) { return withdrawPlayer(player, amount); }

    @Override public EconomyResponse depositPlayer(String s, double v) { return null; }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Cannot deposit negative funds");
        }

        try {
            UUID uuid = player.getUniqueId();
            double currentBalance = getBalanceAsync(uuid).get(5, TimeUnit.SECONDS);
            double newBalance = currentBalance + amount;
            setBalanceAsync(uuid, player.getName(), newBalance).get(5, TimeUnit.SECONDS);
            return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, null);
        } catch (Exception e) {
            plugin.getLogger().severe("[Economy] Deposit failed for " + player.getName() + ": " + e.getMessage());
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Database error");
        }
    }

    @Override public EconomyResponse depositPlayer(String s, String s1, double v) { return null; }
    @Override public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) { return depositPlayer(player, amount); }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return setBalanceAsync(player.getUniqueId(), player.getName(), 0.0).thenApply(v -> true).exceptionally(ex -> false).join();
    }

    @Override public boolean createPlayerAccount(String s, String s1) { return false; }
    @Override public boolean createPlayerAccount(OfflinePlayer player, String worldName) { return createPlayerAccount(player); }

    // Unimplemented bank methods
    @Override public EconomyResponse createBank(String name, String player) { return notSupported(); }
    @Override public EconomyResponse createBank(String s, OfflinePlayer offlinePlayer) { return notSupported(); }
    @Override public EconomyResponse deleteBank(String name) { return notSupported(); }
    @Override public EconomyResponse bankBalance(String name) { return notSupported(); }
    @Override public EconomyResponse bankHas(String name, double amount) { return notSupported(); }
    @Override public EconomyResponse bankWithdraw(String name, double amount) { return notSupported(); }
    @Override public EconomyResponse bankDeposit(String name, double amount) { return notSupported(); }
    @Override public EconomyResponse isBankOwner(String name, String playerName) { return notSupported(); }
    @Override public EconomyResponse isBankOwner(String s, OfflinePlayer offlinePlayer) { return notSupported(); }
    @Override public EconomyResponse isBankMember(String name, String playerName) { return notSupported(); }
    @Override public EconomyResponse isBankMember(String s, OfflinePlayer offlinePlayer) { return notSupported(); }
    @Override public List<String> getBanks() { return List.of(); }

    @Override public boolean createPlayerAccount(String s) { return false; }

    private EconomyResponse notSupported() {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks not supported");
    }
}