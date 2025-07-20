package serveressentials.serveressentials;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class ShopItem {
    private final ItemStack item;
    private final double buyPrice;
    private final double sellPrice;
    private final int slot;
    private final String section;
    private final int page;
    private final EntityType mobType; // null if not a spawner item

    // Constructor for normal items (no mobType)
    public ShopItem(ItemStack item, double buyPrice, double sellPrice, int slot, String section, int page) {
        this.item = item;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.slot = slot;
        this.section = section;
        this.page = page;
        this.mobType = null;
    }

    // Constructor for spawner items with mob type
    public ShopItem(ItemStack item, double buyPrice, double sellPrice, int slot, String section, int page, EntityType mobType) {
        this.item = item;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.slot = slot;
        this.section = section;
        this.page = page;
        this.mobType = mobType;
    }


    public ItemStack getItem() {
        return item;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public int getSlot() {
        return slot;
    }

    public String getSection() {
        return section;
    }

    public int getPage() {
        return page;
    }

    /**
     * Returns the mob type for this item if it's a spawner.
     * Returns null otherwise.
     */
    public EntityType getMobType() {
        return mobType;
    }

    /**
     * Returns true if this ShopItem is a spawner with a mob type.
     */
    public boolean isSpawner() {
        return mobType != null;
    }
}
