package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class WarpGUIListener implements Listener {

    @EventHandler
    public void onWarpClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        String title = ChatColor.stripColor(event.getView().getTitle());
        ItemStack clicked = event.getCurrentItem();
        event.setCancelled(true);

        // === Main category menu ===
        if (title.equalsIgnoreCase("Warp Categories")) {
            String category = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            player.openInventory(WarpGUI.getCategoryGUI(category));
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            return;
        }

        // === Warp menu ===
        if (title.startsWith("Warps:")) {
            String warpName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            WarpData data = WarpManager.getWarpData(warpName);

            if (data == null) {
                player.sendMessage(ChatColor.RED + "Warp not found.");
                return;
            }

            if (!data.isEnabled()) {
                player.sendMessage(ChatColor.RED + "This warp is currently disabled.");
                return;
            }

            // Cooldown check
            long remaining = WarpCooldowns.getRemainingCooldown(player.getUniqueId(), warpName);
            if (remaining > 0) {
                player.sendMessage(ChatColor.RED + "You must wait " + ChatColor.YELLOW + remaining + "s" + ChatColor.RED + " before using this warp again.");
                return;
            }

            // Warp!
            player.teleport(data.getLocation());
            player.sendMessage(ChatColor.GREEN + "Warped to " + ChatColor.YELLOW + warpName + ChatColor.GREEN + "!");
            WarpCooldowns.setCooldown(player.getUniqueId(), warpName, data.getCooldownSeconds());
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
        }
    }
}
