package net.godlycow.org.economy.shop.gui.trigger;

import net.godlycow.org.economy.shop.gui.ShopGUIManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

public class ShopGUIListener implements Listener {
    private final ShopGUIManager guiManager;
    private final Plugin plugin;

    public ShopGUIListener(ShopGUIManager guiManager, Plugin plugin) {
        this.guiManager = guiManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null) return;

        if (guiManager.getQuantitySelectorGUI().isGUIOpen(player.getUniqueId())) {
            event.setCancelled(true);
            guiManager.getQuantitySelectorGUI().handleClick(player, event.getSlot());
            return;
        }

        if (!guiManager.isShopGUIOpen(player.getUniqueId())) {
            return;
        }

        if (event.getClickedInventory() == event.getView().getTopInventory()) {
            event.setCancelled(true);
            guiManager.handleClick(player, event.getSlot(), event.getClick().name(), event.getInventory());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!guiManager.getQuantitySelectorGUI().isGUIOpen(player.getUniqueId())) {
                guiManager.getQuantitySelectorGUI().cleanupPlayer(player.getUniqueId());
            }

            if (!guiManager.isShopGUIOpen(player.getUniqueId())) {
                guiManager.cleanupPlayer(player.getUniqueId());
            }
        }, 2L);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (guiManager.getQuantitySelectorGUI().isAwaitingCustomAmount(player.getUniqueId())) {
            event.setCancelled(true);
            guiManager.getQuantitySelectorGUI().handleChatInput(player, event.getMessage());
        }
    }
}