package net.lunark.io.api;

import com.serveressentials.api.ServerEssentialsAPI;
import com.serveressentials.api.VaultAPI;
import net.lunark.io.vault.VaultManager;
import net.lunark.io.vault.VaultStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

/**
 * Stable, thread-safe implementation of ServerEssentials API.
 * INTERNAL USE ONLY - NOT part of public API.
 */
public final class APIImpl implements ServerEssentialsAPI, VaultAPI {
    private static volatile APIImpl instance;
    private static final Object LOCK = new Object();

    private final JavaPlugin plugin;
    private final VaultManager vaultManager;
    private final VaultStorage vaultStorage;
    private final ConcurrentHashMap<String, ReentrantLock> vaultLocks = new ConcurrentHashMap<>();

    /**
     * PRIVATE constructor - use initialize() instead
     */
    private APIImpl(@NotNull JavaPlugin plugin,
                    @NotNull VaultManager vaultManager,
                    @NotNull VaultStorage vaultStorage) {
        this.plugin = plugin;
        this.vaultManager = vaultManager;
        this.vaultStorage = vaultStorage;
    }

    /**
     * ✅ STATIC PUBLIC INITIALIZER - Call this from your main plugin
     */
    public static void initialize(@NotNull JavaPlugin plugin,
                                  @NotNull VaultManager vaultManager,
                                  @NotNull VaultStorage vaultStorage) {
        synchronized (LOCK) {
            if (instance != null) {
                throw new IllegalStateException("APIImpl already initialized!");
            }
            instance = new APIImpl(plugin, vaultManager, vaultStorage);
            plugin.getLogger().info("ServerEssentialsAPI v" + ServerEssentialsAPI.API_VERSION + " initialized");
        }
    }

    /**
     * Gets the singleton instance
     */
    @NotNull
    public static APIImpl getInstance() {
        APIImpl api = instance;
        if (api == null) {
            throw new IllegalStateException("ServerEssentials API is not loaded!");
        }
        return api;
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
            throw new IllegalArgumentException("Vault number must be 1-10");
        }

        String perm = "serveressentials.command.pv." + vaultNumber;
        if (player.hasPermission(perm)) {
            vaultManager.openVault(player, vaultNumber);
            return true;
        }
        return false;
    }

    @Override
    @NotNull
    public CompletableFuture<Inventory> getVaultInventory(@NotNull UUID playerUUID, int vaultNumber) {
        if (!VaultAPI.isValidVaultNumber(vaultNumber)) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid vault number: " + vaultNumber));
        }

        return vaultStorage.load(playerUUID, vaultNumber)
                .thenApply(optData -> {
                    Inventory inv = Bukkit.createInventory(null, 54, "Vault #" + vaultNumber);
                    if (optData.isPresent()) {
                        vaultStorage.deserializeInto(optData.get(), inv);
                    }
                    return inv;
                });
    }

    @Override
    @NotNull
    public CompletableFuture<Void> saveVault(@NotNull UUID playerUUID, int vaultNumber, @NotNull Inventory inventory) {
        if (!VaultAPI.isValidVaultNumber(vaultNumber)) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid vault number: " + vaultNumber));
        }

        return vaultStorage.save(playerUUID, vaultNumber, inventory);
    }

    @Override
    @NotNull
    public CompletableFuture<Boolean> hasVault(@NotNull UUID playerUUID, int vaultNumber) {
        if (!VaultAPI.isValidVaultNumber(vaultNumber)) {
            return CompletableFuture.completedFuture(false);
        }
        return vaultStorage.exists(playerUUID, vaultNumber);
    }

    @Override
    @NotNull
    public CompletableFuture<Void> clearVault(@NotNull UUID playerUUID, int vaultNumber) {
        if (!VaultAPI.isValidVaultNumber(vaultNumber)) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid vault number: " + vaultNumber));
        }
        return vaultStorage.delete(playerUUID, vaultNumber);
    }

    @Override
    @NotNull
    public CompletableFuture<Integer> getVaultCount(@NotNull UUID playerUUID) {
        CompletableFuture<Boolean>[] futures = new CompletableFuture[MAX_VAULTS];
        for (int i = 0; i < MAX_VAULTS; i++) {
            futures[i] = hasVault(playerUUID, i + 1);
        }

        return CompletableFuture.allOf(futures).thenApply(v -> {
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
}