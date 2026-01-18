package net.godlycow.org.economy.shop.gui;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class ShopInventoryHolder implements InventoryHolder {
    private final ShopGUIManager guiManager;
    private final boolean isMainGUI;

    public ShopInventoryHolder(ShopGUIManager guiManager, boolean isMainGUI) {
        this.guiManager = guiManager;
        this.isMainGUI = isMainGUI;
    }

    public ShopGUIManager getGUIManager() {
        return guiManager;
    }

    public boolean isMainGUI() {
        return isMainGUI;
    }

    @Override
    public Inventory getInventory() {
        return Bukkit.createInventory(this, 54);
    }

}