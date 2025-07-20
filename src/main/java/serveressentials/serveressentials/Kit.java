package serveressentials.serveressentials;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Kit {
    private final String name;
    private final String permission;
    private final String displayName;
    private final Material displayMaterial;
    private final List<String> displayLore;
    private final int slot;
    private final List<ItemStack> items;

    public Kit(String name, String permission, String displayName, Material displayMaterial, List<String> displayLore, int slot, List<ItemStack> items) {
        this.name = name;
        this.permission = permission;
        this.displayName = displayName;
        this.displayMaterial = displayMaterial;
        this.displayLore = displayLore;
        this.slot = slot;
        this.items = items;
    }

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getDisplayMaterial() {
        return displayMaterial;
    }

    public List<String> getDisplayLore() {
        return displayLore;
    }

    public int getSlot() {
        return slot;
    }

    public List<ItemStack> getItems() {
        return items;
    }
}
