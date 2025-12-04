package net.lunark.io.economy;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class ShopGUIListener implements Listener {
    private final ShopGUIManager guiManager;

    public ShopGUIListener(ShopGUIManager guiManager) {
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null || event.getCurrentItem() == null) return;

        // Only handle top inventory clicks
        if (event.getClickedInventory() == event.getView().getTopInventory()) {
            event.setCancelled(true);
            guiManager.handleClick(player, event.getSlot(), event.getClick().name(), event.getInventory());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        // Cleanup can be handled by ShopStorage if needed
    }
}