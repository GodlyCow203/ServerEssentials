package net.lunark.io.vault;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.entity.Player;

public class VaultListener implements Listener {
    private final VaultManager vaultManager;

    public VaultListener(VaultManager vaultManager) {
        this.vaultManager = vaultManager;
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            vaultManager.onVaultClose(player, event.getInventory());
        }
    }
}