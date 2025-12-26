package net.godlycow.org.commands.config;

import org.bukkit.plugin.Plugin;

public final class DisposalConfig {
    private final int inventorySize;
    private final boolean clearOnClose;

    public DisposalConfig(Plugin plugin) {
        this.inventorySize = plugin.getConfig().getInt("disposal.inventory-size", 54);
        this.clearOnClose = plugin.getConfig().getBoolean("disposal.clear-on-close", true);
    }

    public int inventorySize() { return inventorySize; }
    public boolean clearOnClose() { return clearOnClose; }
}