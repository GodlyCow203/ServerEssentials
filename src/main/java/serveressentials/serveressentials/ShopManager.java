package serveressentials.serveressentials;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ShopManager {

    private static final Map<String, List<ShopItem>> sections = new HashMap<>();

    // Add item to the appropriate section
    public static void addItem(ShopItem item) {
        sections.computeIfAbsent(item.getSection(), k -> new ArrayList<>()).add(item);
    }

    // Get all items in a specific section and page
    public static List<ShopItem> getItems(String section, int page) {
        List<ShopItem> all = sections.getOrDefault(section, new ArrayList<>());
        List<ShopItem> filtered = new ArrayList<>();
        for (ShopItem item : all) {
            if (item.getPage() == page) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    // Fallback method to get ALL items from a section (any page)
    public static List<ShopItem> getItems(String section) {
        return sections.getOrDefault(section, new ArrayList<>());
    }

    // Get all available sections
    public static Set<String> getSections() {
        return sections.keySet();
    }

    // Get max page number in a section
    public static int getMaxPages(String section) {
        int max = 1;
        for (ShopItem item : getItems(section)) {
            if (item.getPage() > max) {
                max = item.getPage();
            }
        }
        return max;
    }

    // Load from config
    public static void loadShopItems() {
        ShopConfigManager.loadShopItems();
    }

    // Save to config
    public static void saveShopItems() {
        ShopConfigManager.saveShopItems();
    }

    // Get sell price of an item based on its material
    public static double getSellPrice(Material type) {
        ShopItem item = findItemByType(type);
        return item != null ? item.getSellPrice() : 0.0;
    }

    // Find item by material (ignores page/section)
    public static ShopItem findItemByType(Material type) {
        for (List<ShopItem> list : sections.values()) {
            for (ShopItem item : list) {
                if (item.getItem().getType() == type) {
                    return item;
                }
            }
        }
        return null;
    }

    // Duplicate of addItem
    public static void addShopItem(ShopItem item) {
        addItem(item);
    }

    // Clear all shop items
    public static void clearItems() {
        sections.clear();
    }

    // ✅ Handle item purchase using EconomyManager
    public static boolean buyItem(Player player, ShopItem item) {
        double price = item.getBuyPrice();
        double balance = EconomyManager.getBalance(player);

        if (balance >= price) {
            EconomyManager.takeBalance(player.getUniqueId(), price);
            player.getInventory().addItem(item.getItem().clone());
            player.sendMessage("§aYou bought " + item.getItem().getType() + " for $" + price);
            return true;
        } else {
            player.sendMessage("§cYou don't have enough money to buy this item.");
            return false;
        }
    }
}
