package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class WarpInventoryClickListener implements Listener {

    private final String inventoryTitle;

    public WarpInventoryClickListener() {
        // Must match the inventory title exactly (with color codes)
        this.inventoryTitle = ChatColor.translateAlternateColorCodes('&',
                ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r") + "Available Warps");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(inventoryTitle)) {
            event.setCancelled(true); // Prevent taking the item

            if (event.getCurrentItem() == null) return;
            ItemStack clickedItem = event.getCurrentItem();

            if (!clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) return;

            Player player = (Player) event.getWhoClicked();
            String warpName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName()).toLowerCase();

            if (!WarpManager.warpExists(warpName)) {
                player.sendMessage(inventoryTitle + ChatColor.RED + "Warp does not exist.");
                player.closeInventory();
                return;
            }

            player.closeInventory();
            player.teleport(WarpManager.getWarp(warpName));
            player.sendMessage(ChatColor.GREEN + "Teleported to warp " + ChatColor.YELLOW + warpName + ChatColor.GREEN + ".");
        }
    }
}