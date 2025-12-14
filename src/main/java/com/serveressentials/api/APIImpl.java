package com.serveressentials.api;

import net.lunark.io.vault.VaultManager;
import net.lunark.io.vault.VaultStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;


public final class APIImpl implements ServerEssentialsAPI, VaultAPI {
    private static final ReentrantLock INSTANCE_LOCK = new ReentrantLock();
    private static volatile APIImpl instance;

    private final JavaPlugin plugin;
    private final VaultManager vaultManager;
    private final VaultStorage vaultStorage;

    private final ConcurrentHashMap<String, ReentrantLock> playerVaultLocks = new ConcurrentHashMap<>();

    public APIImpl(@NotNull JavaPlugin plugin,
                   @NotNull VaultManager vaultManager,
                   @NotNull VaultStorage vaultStorage) {
        this.plugin = plugin;
        this.vaultManager = vaultManager;
        this.vaultStorage = vaultStorage;

        INSTANCE_LOCK.lock();
        try {
            if (instance != null) {
                throw new IllegalStateException("API already initialized!");
            }
            instance = this;
        } finally {
            INSTANCE_LOCK.unlock();
        }

        plugin.getLogger().info("[API] ServerEssentialsAPI v" + ServerEssentialsAPI.API_VERSION + " initialized");
    }


    @Nullable
    public static APIImpl getInstance() {
        return instance;
    }

    @Override
    public boolean isAvailable() {
        return plugin.isEnabled();
    }


    @Override
    @NotNull
    public VaultAPI getVaults() {
        return this;
    }


    @Override
    public boolean openVault(@NotNull Player player, int vaultNumber) {
        if (!VaultAPI.isValidVaultNumber(vaultNumber)) {
            throw new IllegalArgumentException("Vault number must be 1-10, got: " + vaultNumber);
        }
        if (!isAvailable()) {
            plugin.getLogger().warning("[API] Attempted to open vault while API is unavailable");
            return false;
        }

        final String perm = "serveressentials.command.pv." + vaultNumber;
        if (player.hasPermission(perm)) {
            try {
                vaultManager.openVault(player, vaultNumber);
                plugin.getLogger().fine("[API] Opened vault " + vaultNumber + " for " + player.getName());
                return true;
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "[API] Failed to open vault for " + player.getName(), e);
                player.sendMessage("§cAn error occurred while opening your vault. Please contact staff.");
                return false;
            }
        }
        return false;
    }

    @Override
    @NotNull
    public CompletableFuture<Inventory> getVaultInventory(@NotNull UUID playerUUID, int vaultNumber) {
        if (!VaultAPI.isValidVaultNumber(vaultNumber)) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Invalid vault number: " + vaultNumber)
            );
        }
        if (!isAvailable()) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("API is not available")
            );
        }

        // Acquire per-player lock to prevent concurrent access
        final ReentrantLock lock = getPlayerLock(playerUUID, vaultNumber);
        lock.lock();

        try {
            return vaultStorage.load(playerUUID, vaultNumber)
                    .thenApply(optData -> {
                        Inventory inv = Bukkit.createInventory(null, 54, "Vault #" + vaultNumber);
                        if (optData.isPresent()) {
                            vaultStorage.deserializeInto(optData.get(), inv);
                        }
                        return inv;
                    })
                    .exceptionally(ex -> {
                        plugin.getLogger().log(Level.SEVERE, "[API] Failed to load vault for " + playerUUID, ex);
                        return Bukkit.createInventory(null, 54, "Error Loading Vault");
                    })
                    .whenComplete((inv, ex) -> {
                        lock.unlock();
                    });
        } catch (Exception e) {
            lock.unlock();
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    @NotNull
    public CompletableFuture<Void> saveVault(@NotNull UUID playerUUID, int vaultNumber, @NotNull Inventory inventory) {
        if (!VaultAPI.isValidVaultNumber(vaultNumber)) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Invalid vault number: " + vaultNumber)
            );
        }
        if (!isAvailable()) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("API is not available")
            );
        }

        final ReentrantLock lock = getPlayerLock(playerUUID, vaultNumber);
        lock.lock();

        try {
            return vaultStorage.save(playerUUID, vaultNumber, inventory)
                    .exceptionally(ex -> {
                        plugin.getLogger().log(Level.SEVERE, "[API] Failed to save vault for " + playerUUID, ex);
                        throw new RuntimeException("Save failed", ex);
                    })
                    .whenComplete((v, ex) -> lock.unlock());
        } catch (Exception e) {
            lock.unlock();
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    @NotNull
    public CompletableFuture<Boolean> hasVault(@NotNull UUID playerUUID, int vaultNumber) {
        if (!VaultAPI.isValidVaultNumber(vaultNumber)) {
            return CompletableFuture.completedFuture(false);
        }
        if (!isAvailable()) {
            return CompletableFuture.completedFuture(false);
        }

        return vaultStorage.exists(playerUUID, vaultNumber)
                .exceptionally(ex -> {
                    plugin.getLogger().log(Level.SEVERE, "[API] Failed to check vault existence for " + playerUUID, ex);
                    return false;
                });
    }

    @Override
    @NotNull
    public CompletableFuture<Void> clearVault(@NotNull UUID playerUUID, int vaultNumber) {
        if (!VaultAPI.isValidVaultNumber(vaultNumber)) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Invalid vault number: " + vaultNumber)
            );
        }
        if (!isAvailable()) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("API is not available")
            );
        }

        final ReentrantLock lock = getPlayerLock(playerUUID, vaultNumber);
        lock.lock();

        try {
            return vaultStorage.delete(playerUUID, vaultNumber)
                    .thenRun(() -> plugin.getLogger().info("[API] Cleared vault " + vaultNumber + " for " + playerUUID))
                    .exceptionally(ex -> {
                        plugin.getLogger().log(Level.SEVERE, "[API] Failed to clear vault for " + playerUUID, ex);
                        throw new RuntimeException("Clear failed", ex);
                    })
                    .whenComplete((v, ex) -> lock.unlock());
        } catch (Exception e) {
            lock.unlock();
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    @NotNull
    public CompletableFuture<Integer> getVaultCount(@NotNull UUID playerUUID) {
        if (!isAvailable()) {
            return CompletableFuture.completedFuture(0);
        }

        CompletableFuture<Boolean>[] futures = new CompletableFuture[MAX_VAULTS];
        for (int i = 0; i < MAX_VAULTS; i++) {
            futures[i] = hasVault(playerUUID, i + 1);
        }

        return CompletableFuture.allOf(futures)
                .thenApply(v -> {
                    int count = 0;
                    for (CompletableFuture<Boolean> future : futures) {
                        try {
                            if (future.get()) count++;
                        } catch (Exception e) {
                            plugin.getLogger().log(Level.WARNING, "[API] Error counting vaults for " + playerUUID, e);
                        }
                    }
                    return count;
                });
    }

    /**
     * Gets or creates a lock for specific player vault to prevent concurrent access
     */
    @NotNull
    private ReentrantLock getPlayerLock(@NotNull UUID playerUUID, int vaultNumber) {
        final String key = playerUUID.toString() + ":" + vaultNumber;
        return playerVaultLocks.computeIfAbsent(key, k -> new ReentrantLock());
    }

    /**
     * Cleans up expired locks (called periodically from main plugin)
     */
    void cleanupLocks() {
        playerVaultLocks.entrySet().removeIf(entry -> {
            ReentrantLock lock = entry.getValue();
            return !lock.isLocked() && lock.getQueueLength() == 0;
        });
    }
}