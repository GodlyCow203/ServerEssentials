package net.lunark.io.sellgui;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class SellGUIListener implements Listener {
    private final SellGUIManager guiManager;
    private final MiniMessage mini = MiniMessage.miniMessage();

    public SellGUIListener(SellGUIManager guiManager) {
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Check if this is our sell GUI
        Inventory topInv = event.getView().getTopInventory();
        if (topInv == null || !event.getView().title().toString().contains("Sell Items")) {
            return;
        }

        // BLOCK putting non-sellable items into GUI
        if (event.getView().getBottomInventory() == event.getClickedInventory() &&
                event.getCursor() != null && event.getCursor().getType() != org.bukkit.Material.AIR) {

            ItemStack cursor = event.getCursor();
            if (!guiManager.isSellable(cursor.getType())) {
                player.sendMessage(mini.deserialize("<red>❌ This item cannot be sold!"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1f);
                event.setCancelled(true);
                return;
            }
        }

        // Allow moving sellable items into GUI
        if (event.getClickedInventory() == topInv) {
            // Allow any item movement in GUI - we'll process on close
            return;
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Inventory topInv = event.getView().getTopInventory();
        if (topInv == null || !event.getView().title().toString().contains("Sell Items")) {
            return;
        }

        // Check if dragging non-sellable items into GUI
        if (event.getRawSlots().stream().anyMatch(slot -> slot < topInv.getSize())) {
            ItemStack item = event.getOldCursor();
            if (item != null && !guiManager.isSellable(item.getType())) {
                player.sendMessage(mini.deserialize("<red>❌ This item cannot be sold!"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1f);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        Inventory inv = event.getInventory();
        if (inv == null || !event.getView().title().toString().contains("Sell Items")) {
            return;
        }

        // Process items on GUI close
        guiManager.processSellAndReturnItems(player, inv);
    }
}