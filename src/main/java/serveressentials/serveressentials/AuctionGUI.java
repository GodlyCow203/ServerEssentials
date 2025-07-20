

package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;

import java.util.*;
import java.util.regex.Pattern;

public class AuctionGUI {
    private final AuctionManager manager;
    private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");

    public AuctionGUI(AuctionManager manager) {
        this.manager = manager;
    }

    public void open(Player player, int page) {
        List<AuctionItem> items = manager.getItems();
        List<Integer> centerSlots = List.of(20, 21, 22, 23, 24, 29, 30, 31, 32, 33);
        int itemsPerPage = centerSlots.size();
        int totalPages = (int) Math.ceil((double) items.size() / itemsPerPage);
        page = Math.max(0, Math.min(page, totalPages - 1));

        Inventory gui = Bukkit.createInventory(null, 54, translateColor("<#00FF00>Auction House | Page " + (page + 1)));

        ItemStack glass = createGlassPane();

        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                gui.setItem(i, glass);
            }
        }

        int start = page * itemsPerPage;
        int end = Math.min(start + itemsPerPage, items.size());
        List<AuctionItem> pageItems = items.subList(start, end);

        for (int i = 0; i < pageItems.size(); i++) {
            AuctionItem auctionItem = pageItems.get(i);
            ItemStack item = auctionItem.getItem().clone();
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                NamespacedKey sellerKey = new NamespacedKey(ServerEssentials.getInstance(), "auction-seller");
                NamespacedKey idKey = new NamespacedKey(ServerEssentials.getInstance(), "auction-id");

                meta.getPersistentDataContainer().set(sellerKey, PersistentDataType.STRING, auctionItem.getSeller().toString());
                meta.getPersistentDataContainer().set(idKey, PersistentDataType.INTEGER, auctionItem.getId());

                List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                lore.add(translateColor("<#FFD700>Price: $" + auctionItem.getPrice()));
                lore.add(translateColor("<#808080>Click to purchase"));
                meta.setLore(lore);
                item.setItemMeta(meta);
            }

            gui.setItem(centerSlots.get(i), item);
        }

        if (page > 0) {
            gui.setItem(45, navItem(Material.ARROW, translateColor("<#FFFF00>Previous Page")));
        }
        gui.setItem(49, navItem(Material.BARRIER, translateColor("<#FF0000>Close")));
        if (page < totalPages - 1) {
            gui.setItem(53, navItem(Material.ARROW, translateColor("<#FFFF00>Next Page")));
        }

        player.openInventory(gui);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
    }

    private ItemStack createGlassPane() {
        ItemStack glass = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(translateColor("<#808080> "));
        glass.setItemMeta(glassMeta);
        return glass;
    }

    private ItemStack navItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private String translateColor(String message) {
        return HEX_PATTERN.matcher(message).replaceAll(match -> {
            return "§x" + match.group(1).chars().mapToObj(c -> "§" + (char) c).reduce("", String::concat);
        });
    }
}
