package com.serveressentials.api.shop;

import org.bukkit.Material;
import java.util.Collections;
import java.util.List;


public class ShopButton {
    private final Material material;
    private final String name;
    private final List<String> lore;
    private final String file;

    public ShopButton(Material material, String name, List<String> lore, String file) {
        this.material = material;
        this.name = name;
        this.lore = List.copyOf(lore != null ? lore : List.of());
        this.file = file;
    }

    public Material getMaterial() { return material; }
    public String getName() { return name; }
    public List<String> getLore() { return Collections.unmodifiableList(lore); }
    public String getFile() { return file; }
}