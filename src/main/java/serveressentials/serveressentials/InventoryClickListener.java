package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;

public class InventoryClickListener implements Listener {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (!ChatColor.stripColor(event.getView().getTitle()).equals("Available Warps")) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String warpName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        Location warpLoc = WarpManager.getWarp(warpName);

        if (warpLoc != null) {
            player.closeInventory();
            player.sendMessage(getPrefix() + ChatColor.GREEN + "Teleporting to " + ChatColor.YELLOW + warpName + ChatColor.GREEN + "...");
            player.teleport(warpLoc);
        } else {
            player.sendMessage(getPrefix() + ChatColor.RED + "Warp not found.");
        }
    }
}
