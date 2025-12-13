package com.serveressentials.api;

import net.lunark.io.vault.VaultManager;
import net.lunark.io.vault.VaultStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class APIImpl implements ServerEssentialsAPI, VaultAPI {
    private final VaultManager vaultManager;
    private final VaultStorage vaultStorage;
    private final JavaPlugin plugin;

    public APIImpl(JavaPlugin plugin, VaultManager vaultManager, VaultStorage vaultStorage) {
        this.plugin = plugin;
        this.vaultManager = vaultManager;
        this.vaultStorage = vaultStorage;

        // Register this instance
        ServerEssentialsAPI.provide(this);
    }

    @Override
    public VaultAPI getVaults() {
        return this;
    }

    @Override
    public boolean isAvailable() {
        return plugin.isEnabled();
    }

    @Override
    public boolean openVault(Player player, int vaultNumber) {
        if (!isValidVaultNumber(vaultNumber)) {
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
    public CompletableFuture<Inventory> getVaultInventory(UUID playerUUID, int vaultNumber) {
        if (!isValidVaultNumber(vaultNumber)) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Vault number must be 1-10")
            );
        }

        return vaultStorage.loadVaultData(playerUUID, vaultNumber)
                .thenApply(optData -> {
                    Inventory inv = Bukkit.createInventory(null, 54, "Vault #" + vaultNumber);
                    if (optData.isPresent()) {
                        vaultStorage.deserializeInventory(inv, optData.get());
                    }
                    return inv;
                });
    }

    @Override
    public CompletableFuture<Void> saveVault(UUID playerUUID, int vaultNumber, Inventory inventory) {
        if (!isValidVaultNumber(vaultNumber)) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Vault number must be 1-10")
            );
        }
        return vaultStorage.saveVault(playerUUID, vaultNumber, inventory);
    }

    @Override
    public CompletableFuture<Boolean> hasVault(UUID playerUUID, int vaultNumber) {
        if (!isValidVaultNumber(vaultNumber)) {
            return CompletableFuture.completedFuture(false);
        }
        return vaultStorage.hasVault(playerUUID, vaultNumber);
    }

    @Override
    public CompletableFuture<Void> clearVault(UUID playerUUID, int vaultNumber) {
        if (!isValidVaultNumber(vaultNumber)) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Vault number must be 1-10")
            );
        }
        return vaultStorage.clearVault(playerUUID, vaultNumber);
    }

    private boolean isValidVaultNumber(int number) {
        return number >= 1 && number <= MAX_VAULTS;
    }

    public static void initialize(JavaPlugin plugin, VaultManager manager, VaultStorage storage) {
        new APIImpl(plugin, manager, storage);
    }
}