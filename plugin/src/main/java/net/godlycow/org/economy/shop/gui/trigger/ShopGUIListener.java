package net.godlycow.org.economy.shop.gui.trigger;

import net.godlycow.org.economy.shop.gui.QuantityInventoryHolder;
import net.godlycow.org.economy.shop.gui.ShopGUIManager;
import net.godlycow.org.economy.shop.gui.ShopInventoryHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

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

        if (event.getView().getTopInventory().getHolder() instanceof QuantityInventoryHolder) {
            event.setCancelled(true);
            guiManager.getQuantitySelectorGUI().handleClick(player, event.getSlot());
            return;
        }


        if (!(event.getView().getTopInventory().getHolder() instanceof ShopInventoryHolder)) {
            return;
        }

        event.setCancelled(true);
        guiManager.handleClick(player, event.getSlot(), event.getClick().name(), event.getInventory());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        Inventory inv = event.getInventory();

        if (inv.getHolder() instanceof QuantityInventoryHolder) {
            UUID uuid = player.getUniqueId();

            if (!guiManager.getQuantitySelectorGUI().isAwaitingCustomAmount(uuid)) {
                guiManager.getQuantitySelectorGUI().cleanupGUI(uuid);
            }
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            UUID playerId = player.getUniqueId();

            boolean inQuantityGUI = guiManager.getQuantitySelectorGUI().isGUIOpen(playerId);
            boolean inShopGUI = guiManager.isShopGUIOpen(playerId);

            if (!inQuantityGUI && !inShopGUI) {
                guiManager.cleanupPlayer(playerId);
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