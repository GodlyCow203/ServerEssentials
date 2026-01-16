package net.godlycow.org.settings;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public final class SettingsGUIHolder implements InventoryHolder {
    private final int page;

    public SettingsGUIHolder(int page) {
        this.page = page;
    }

    public int getPage() {
        return page;
    }

    @Override
    public Inventory getInventory() {
        throw new UnsupportedOperationException("SettingsGUIHolder doesn't store Inventory directly");
    }
}