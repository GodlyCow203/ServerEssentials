package net.lunark.io.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ToolMoveFixListener implements Listener {

    private final JavaPlugin plugin;

    public ToolMoveFixListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getType().name().equalsIgnoreCase("ANVIL") && event.getRawSlot() == 2) return;

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType().isAir()) return;

        if (event.isCancelled() && canForceAllow(event)) {
            Bukkit.getScheduler().runTask(plugin, () -> event.setCancelled(false));
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.isCancelled()) {
            Bukkit.getScheduler().runTask(plugin, () -> event.setCancelled(false));
        }
    }

    private boolean canForceAllow(InventoryClickEvent event) {
        return event.getClickedInventory() != null
                && event.getWhoClicked().getGameMode() != org.bukkit.GameMode.CREATIVE;
    }
}
