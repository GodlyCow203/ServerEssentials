package net.godlycow.org.kit.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class KitInventoryHolder implements InventoryHolder {
    private final KitGUIManager guiManager;
    private final boolean isPreviewGUI;

    public KitInventoryHolder(KitGUIManager guiManager, boolean isPreviewGUI) {
        this.guiManager = guiManager;
        this.isPreviewGUI = isPreviewGUI;
    }

    public KitGUIManager getGUIManager() {
        return guiManager;
    }

    public boolean isPreviewGUI() {
        return isPreviewGUI;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}