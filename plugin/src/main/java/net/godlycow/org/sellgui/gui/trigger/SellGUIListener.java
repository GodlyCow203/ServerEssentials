package net.godlycow.org.sellgui.gui.trigger;

import net.godlycow.org.sellgui.gui.SellGUIManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SellGUIListener implements Listener {
    private final SellGUIManager guiManager;
    private final PlayerLanguageManager langManager;
    private final MiniMessage mini = MiniMessage.miniMessage();

    public SellGUIListener(PlayerLanguageManager langManager, SellGUIManager guiManager) {
        this.langManager = langManager;
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Inventory topInv = event.getView().getTopInventory();
        if (topInv == null || !isSellGUI(event.getView().title().toString())) {
            return;
        }

        if (event.getCurrentItem() != null && guiManager.isDisplayItem(event.getCurrentItem())) {
            event.setCancelled(true);
            return;
        }

        if (event.getView().getBottomInventory() == event.getClickedInventory() &&
                event.getCursor() != null && !event.getCursor().getType().isAir()) {

            ItemStack cursor = event.getCursor();
            if (!guiManager.isSellable(cursor.getType())) {
                player.sendMessage(langManager.getMessageFor(player, "economy.sellgui.not-sellable",
                        "<red>❌ This item cannot be sold!"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1f);
                event.setCancelled(true);
                return;
            }
        }

        if (event.getClick().isShiftClick() && event.getClickedInventory() == topInv) {
            int slot = event.getSlot();
            if (guiManager.isBorderSlot(slot, topInv.getSize())) {
                event.setCancelled(true);
                return;
            }
        }
        updateTotalValue(player, topInv);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Inventory topInv = event.getView().getTopInventory();
        if (topInv == null || !isSellGUI(event.getView().title().toString())) {
            return;
        }

        for (int slot : event.getRawSlots()) {
            if (guiManager.isBorderSlot(slot, topInv.getSize())) {
                event.setCancelled(true);
                return;
            }
        }
        if (event.getRawSlots().stream().anyMatch(slot -> slot < topInv.getSize())) {
            ItemStack item = event.getOldCursor();
            if (item != null && !guiManager.isSellable(item.getType())) {
                player.sendMessage(langManager.getMessageFor(player, "economy.sellgui.not-sellable",
                        "<red>❌ This item cannot be sold!"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1f);
                event.setCancelled(true);
            }
        }

        updateTotalValue(player, topInv);
    }

    private void updateTotalValue(Player player, Inventory inv) {
        double totalValue = 0.0;

        for (int i = 0; i < inv.getSize(); i++) {
            if (guiManager.isBorderSlot(i, inv.getSize())) continue;

            ItemStack item = inv.getItem(i);
            if (item == null || item.getType() == Material.AIR) continue;
            if (guiManager.isDisplayItem(item)) continue;

            double price = guiManager.getSellPrice(item.getType());
            if (price > 0) {
                totalValue += price * item.getAmount();
            }
        }
        guiManager.updateValueDisplay(player, inv, totalValue);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        Inventory inv = event.getInventory();
        if (inv == null || !isSellGUI(event.getView().title().toString())) {
            return;
        }

        guiManager.processSellAndReturnItems(player, inv);
    }


    private boolean isSellGUI(String title) {
        return title.contains("Sell Items");
    }
}