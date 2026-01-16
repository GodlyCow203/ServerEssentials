package com.serveressentials.api.shop;

import org.bukkit.Material;
import java.util.Collections;
import java.util.List;


public class ShopLayout {
    private final Material material;
    private final String name;
    private final List<String> lore;
    private final boolean clickable;

    public ShopLayout(Material material, String name, List<String> lore, boolean clickable) {
        this.material = material;
        this.name = name;
        this.lore = List.copyOf(lore != null ? lore : List.of());
        this.clickable = clickable;
    }

    public Material getMaterial() { return material; }
    public String getName() { return name; }
    public List<String> getLore() { return Collections.unmodifiableList(lore); }
    public boolean isClickable() { return clickable; }
}