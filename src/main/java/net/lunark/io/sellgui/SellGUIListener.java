package net.lunark.io.sellgui;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.lunark.io.language.PlayerLanguageManager;
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
        if (topInv == null || !event.getView().title().toString().contains("Sell Items")) {
            return;
        }

        if (event.getView().getBottomInventory() == event.getClickedInventory() &&
                event.getCursor() != null && event.getCursor().getType() != org.bukkit.Material.AIR) {

            ItemStack cursor = event.getCursor();
            if (!guiManager.isSellable(cursor.getType())) {
                player.sendMessage(langManager.getMessageFor(player, "economy.sellgui.not-sellable",
                        "<red>❌ This item cannot be sold!"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1f);
                event.setCancelled(true);
                return;
            }
        }

        if (event.getClickedInventory() == topInv) {
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

        if (event.getRawSlots().stream().anyMatch(slot -> slot < topInv.getSize())) {
            ItemStack item = event.getOldCursor();
            if (item != null && !guiManager.isSellable(item.getType())) {
                player.sendMessage(langManager.getMessageFor(player, "economy.sellgui.not-sellable",
                        "<red>❌ This item cannot be sold!"));
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

        guiManager.processSellAndReturnItems(player, inv);
    }
}