package net.lunark.io.homes;

public class HomesConfirmHolder implements org.bukkit.inventory.InventoryHolder {
    private final int homeIndex;
    private final String mode;

    public HomesConfirmHolder(int homeIndex, String mode) {
        this.homeIndex = homeIndex;
        this.mode = mode;
    }

    public int getHomeIndex() { return homeIndex; }
    public String getMode() { return mode; }

    @Override
    public org.bukkit.inventory.Inventory getInventory() { return null; }
}
