package net.lunark.io.listeners;

import net.lunark.io.commands.config.DisposalConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public class DisposalListener implements Listener {
    private final DisposalConfig config;

    public DisposalListener(DisposalConfig config) {
        this.config = config;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        String title = event.getView().getTitle();
        if (title.contains("Disposal") && config.clearOnClose()) {
            event.getInventory().clear();
        }
    }
}