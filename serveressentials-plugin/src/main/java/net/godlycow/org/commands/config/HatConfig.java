package net.godlycow.org.commands.config;

import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public final class HatConfig {
    private final Set<Material> allowedItems;
    private final boolean requireAllowedList;

    public HatConfig(Plugin plugin) {
        boolean requireAllowedList1;
        requireAllowedList1 = plugin.getConfig().getBoolean("hat.require-allowed-list", false);

        List<String> itemList = plugin.getConfig().getStringList("hat.allowed-items");
        this.allowedItems = new HashSet<>();

        for (String itemName : itemList) {
            try {
                Material mat = Material.valueOf(itemName.toUpperCase());
                allowedItems.add(mat);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material in hat.allowed-items: " + itemName);
            }
        }

        // If list is empty, allow all items
        if (allowedItems.isEmpty()) {
            requireAllowedList1 = false;
        }
        this.requireAllowedList = requireAllowedList1;
    }

    public boolean isAllowedItem(Material material) {
        return !requireAllowedList || allowedItems.contains(material);
    }
}