package com.serveressentials.api.shop;

import org.bukkit.Material;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Data transfer object for shop items
 */
public class ShopItem {
    private final Material material;
    private final int amount;
    private final String name;
    private final List<String> lore;
    private final double buyPrice;
    private final double sellPrice;
    private final String customItemId;
    private final int slot;
    private final int page;
    private final boolean clickable;

    public ShopItem(Material material, int amount, String name, List<String> lore,
                    double buyPrice, double sellPrice, String customItemId, int slot, int page, boolean clickable) {
        this.material = material;
        this.amount = amount;
        this.name = name;
        this.lore = List.copyOf(lore != null ? lore : List.of());
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.customItemId = customItemId;
        this.slot = slot;
        this.page = page;
        this.clickable = clickable;
    }

    // Getters
    public Material getMaterial() { return material; }
    public int getAmount() { return amount; }
    public String getName() { return name; }
    public List<String> getLore() { return Collections.unmodifiableList(lore); }
    public double getBuyPrice() { return buyPrice; }
    public double getSellPrice() { return sellPrice; }
    public Optional<String> getCustomItemId() { return Optional.ofNullable(customItemId); }
    public int getSlot() { return slot; }
    public int getPage() { return page; }
    public boolean isClickable() { return clickable; }
}