package net.godlycow.org.economy.eco;

import net.godlycow.org.database.DatabaseManager;
import net.godlycow.org.util.logger.AnsiColorUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class InternalEconomy implements EconomyAPI {
    private final Plugin plugin;
    private final DatabaseManager dbManager;
    private static final String POOL_KEY = "economy";

    public InternalEconomy(Plugin plugin, DatabaseManager dbManager) {
        this.plugin = plugin;
        this.dbManager = dbManager;
        initializeDatabase();
    }

    private void initializeDatabase() {
        plugin.getLogger().info(AnsiColorUtil.info("Initializing InternalEconomy database..."));

        String balancesSql = "CREATE TABLE IF NOT EXISTS economy_balances (" +
                "player_uuid TEXT PRIMARY KEY, " +
                "player_name TEXT NOT NULL, " +
                "balance REAL NOT NULL DEFAULT 0.0)";

        String settingsSql = "CREATE TABLE IF NOT EXISTS economy_settings (" +
                "player_uuid TEXT PRIMARY KEY, " +
                "player_name TEXT NOT NULL, " +
                "payments_disabled BOOLEAN NOT NULL DEFAULT FALSE)";

        CompletableFuture<Void> tables = CompletableFuture.allOf(
                dbManager.executeUpdate(POOL_KEY, balancesSql),
                dbManager.executeUpdate(POOL_KEY, settingsSql)
        );

        CompletableFuture<Void> column = tables.thenCompose(v ->
                dbManager.executeQuery(POOL_KEY,
                                "PRAGMA table_info(economy_settings)", rs -> {
                                    while (rs.next()) {
                                        if ("pay_confirm_disabled".equals(rs.getString("name"))) {
                                            return true;
                                        }
                                    }
                                    return false;
                                })
                        .thenCompose(optExists -> {
                            boolean exists = optExists.orElse(false);
                            if (!exists) {
                                plugin.getLogger().info(AnsiColorUtil.warning("Adding pay_confirm_disabled column..."));
                                return dbManager.executeUpdate(POOL_KEY,
                                        "ALTER TABLE economy_settings " +
                                                "ADD COLUMN pay_confirm_disabled BOOLEAN NOT NULL DEFAULT FALSE");
                            }
                            return CompletableFuture.completedFuture(null);
                        })
        );

        try {
            column.join();
            plugin.getLogger().info(AnsiColorUtil.success("InternalEconomy database initialized!"));
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, AnsiColorUtil.danger("Failed to initialize economy database!"), e);
            throw new RuntimeException("Economy initialization failed", e);
        }
    }

    @Override
    public boolean isEnabled() { return true; }

    @Override
    public String getName() { return "ServerEssentials-Internal"; }

    @Override
    public String format(double amount) { return String.format("$%.2f", amount); }


    public boolean hasAccount(OfflinePlayer player) {
        return hasAccount(player.getUniqueId());
    }

    public boolean hasAccount(UUID playerUuid) {
        try {
            return hasAccountAsync(playerUuid).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, AnsiColorUtil.danger(
                    "[InternalEconomy] Failed to check account for UUID " + playerUuid + ": " + e.getMessage()
            ));
            return false;
        }
    }

    private CompletableFuture<Boolean> hasAccountAsync(UUID uuid) {
        return dbManager.executeQuery(POOL_KEY,
                        "SELECT 1 FROM economy_balances WHERE player_uuid = ? LIMIT 1",
                        rs -> rs.next(),
                        uuid.toString()
                ).thenApply(opt -> opt.orElse(false))
                .exceptionally(ex -> {
                    plugin.getLogger().log(Level.SEVERE, AnsiColorUtil.danger(
                            "[InternalEconomy] Account check query failed for UUID: " + uuid
                    ), ex);
                    return false;
                });
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return getBalance(player.getUniqueId());
    }

    public double getBalance(UUID playerUuid) {
        try {
            return getBalanceAsync(playerUuid).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, AnsiColorUtil.danger(
                    "[InternalEconomy] Failed to get balance for UUID " + playerUuid + ": " + e.getMessage()
            ));
            return 0.0;
        }
    }

    private CompletableFuture<Double> getBalanceAsync(UUID uuid) {
        return dbManager.executeQuery(POOL_KEY,
                        "SELECT balance FROM economy_balances WHERE player_uuid = ?",
                        rs -> rs.next() ? rs.getDouble("balance") : 0.0,
                        uuid.toString()
                ).thenApply(opt -> opt.orElse(0.0))
                .exceptionally(ex -> {
                    plugin.getLogger().log(Level.SEVERE, AnsiColorUtil.danger(
                            "[InternalEconomy] Balance query failed for UUID: " + uuid
                    ), ex);
                    return 0.0;
                });
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        if (amount <= 0) return true;
        return getBalance(player) >= amount;
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        UUID uuid = player.getUniqueId();

        if (amount == 0) {
            double balance = getBalance(uuid);
            return new EconomyResponse(0, balance, EconomyResponse.ResponseType.SUCCESS, null);
        }

        if (amount < 0) {
            return new EconomyResponse(0, getBalance(uuid), EconomyResponse.ResponseType.FAILURE,
                    "Cannot deposit negative amount");
        }

        try {
            if (!hasAccount(uuid)) {
                createPlayerAccount(player);
            }

            double currentBalance = getBalanceAsync(uuid).get(5, TimeUnit.SECONDS);
            double newBalance = currentBalance + amount;

            setBalanceAsync(uuid, player.getName(), newBalance).get(5, TimeUnit.SECONDS);

            plugin.getLogger().fine(() -> AnsiColorUtil.success(
                    String.format("[InternalEconomy] Deposited $%.2f to %s. New balance: $%.2f",
                            amount, player.getName(), newBalance)
            ));

            return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, null);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, AnsiColorUtil.danger(
                    "[InternalEconomy] Deposit failed for " + player.getName() + ": " + e.getMessage()
            ), e);
            return new EconomyResponse(0, getBalance(uuid), EconomyResponse.ResponseType.FAILURE,
                    "Database error: " + e.getMessage());
        }
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        UUID uuid = player.getUniqueId();

        if (amount == 0) {
            double balance = getBalance(uuid);
            return new EconomyResponse(0, balance, EconomyResponse.ResponseType.SUCCESS, null);
        }

        if (amount < 0) {
            return new EconomyResponse(0, getBalance(uuid), EconomyResponse.ResponseType.FAILURE,
                    "Cannot withdraw negative amount");
        }

        try {
            double currentBalance = getBalanceAsync(uuid).get(5, TimeUnit.SECONDS);

            if (currentBalance < amount) {
                return new EconomyResponse(amount, currentBalance, EconomyResponse.ResponseType.FAILURE,
                        "Insufficient funds");
            }

            double newBalance = currentBalance - amount;
            setBalanceAsync(uuid, player.getName(), newBalance).get(5, TimeUnit.SECONDS);

            plugin.getLogger().fine(() -> AnsiColorUtil.success(
                    String.format("[InternalEconomy] Withdrew $%.2f from %s. New balance: $%.2f",
                            amount, player.getName(), newBalance)
            ));

            return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, null);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, AnsiColorUtil.danger(
                    "[InternalEconomy] Withdrawal failed for " + player.getName() + ": " + e.getMessage()
            ), e);
            return new EconomyResponse(0, getBalance(uuid), EconomyResponse.ResponseType.FAILURE,
                    "Database error: " + e.getMessage());
        }
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        UUID uuid = player.getUniqueId();

        if (hasAccount(uuid)) {
            plugin.getLogger().fine(() -> AnsiColorUtil.warning(
                    "[InternalEconomy] Account already exists for " + player.getName()
            ));
            return true;
        }

        try {
            setBalanceAsync(uuid, player.getName(), 0.0).join();

            plugin.getLogger().info(AnsiColorUtil.success(
                    String.format("[InternalEconomy] Created account for %s", player.getName())
            ));
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, AnsiColorUtil.danger(
                    "[InternalEconomy] Account creation failed for " + player.getName() + ": " + e.getMessage()
            ), e);
            return false;
        }
    }

    private CompletableFuture<Void> setBalanceAsync(UUID uuid, String name, double amount) {
        return dbManager.executeUpdate(POOL_KEY,
                "INSERT OR REPLACE INTO economy_balances (player_uuid, player_name, balance) VALUES (?, ?, ?)",
                uuid.toString(), name, Math.max(0, amount)
        ).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, AnsiColorUtil.danger(
                    "[InternalEconomy] Failed to set balance for UUID: " + uuid
            ), ex);
            return null;
        });
    }

    @Override
    public CompletableFuture<Boolean> hasPaymentsDisabled(String playerUuid) {
        return dbManager.executeQuery(POOL_KEY,
                        "SELECT payments_disabled FROM economy_settings WHERE player_uuid = ?",
                        rs -> rs.next() && rs.getBoolean("payments_disabled"),
                        playerUuid
                ).thenApply(opt -> opt.orElse(false))
                .exceptionally(ex -> {
                    plugin.getLogger().log(Level.SEVERE, AnsiColorUtil.danger(
                            "[InternalEconomy] Failed to check payments disabled for UUID: " + playerUuid
                    ), ex);
                    return false;
                });
    }

    @Override
    public CompletableFuture<Void> setPaymentsDisabled(String playerUuid, String playerName, boolean disabled) {
        return dbManager.executeUpdate(POOL_KEY,
                "INSERT OR REPLACE INTO economy_settings (player_uuid, player_name, payments_disabled) VALUES (?, ?, ?)",
                playerUuid, playerName, disabled
        ).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, AnsiColorUtil.danger(
                    "[InternalEconomy] Failed to set payments disabled for UUID: " + playerUuid
            ), ex);
            return null;
        });
    }

    @Override
    public CompletableFuture<Boolean> hasPayConfirmDisabled(String playerUuid) {
        return dbManager.executeQuery(POOL_KEY,
                        "SELECT pay_confirm_disabled FROM economy_settings WHERE player_uuid = ?",
                        rs -> rs.next() && rs.getBoolean("pay_confirm_disabled"),
                        playerUuid
                ).thenApply(opt -> opt.orElse(false))
                .exceptionally(ex -> {
                    plugin.getLogger().log(Level.SEVERE, AnsiColorUtil.danger(
                            "[InternalEconomy] Failed to check pay confirm disabled for UUID: " + playerUuid
                    ), ex);
                    return false;
                });
    }

    @Override
    public CompletableFuture<Void> setPayConfirmDisabled(String playerUuid, String playerName, boolean disabled) {
        return dbManager.executeUpdate(POOL_KEY,
                "INSERT OR REPLACE INTO economy_settings (player_uuid, player_name, pay_confirm_disabled, payments_disabled) " +
                        "VALUES (?, ?, ?, COALESCE((SELECT payments_disabled FROM economy_settings WHERE player_uuid = ?), FALSE))",
                playerUuid, playerName, disabled, playerUuid
        ).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, AnsiColorUtil.danger(
                    "[InternalEconomy] Failed to set pay confirm disabled for UUID: " + playerUuid
            ), ex);
            return null;
        });
    }
}