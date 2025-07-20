package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class HomeGUIListener implements Listener {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = ChatColor.stripColor(event.getView().getTitle());
        if (!title.startsWith("Your Homes")) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String displayName = ChatColor.stripColor(meta.getDisplayName());

        // Handle arrow buttons
        int currentPage = 0;
        if (title.contains("Page")) {
            try {
                currentPage = Integer.parseInt(title.replaceAll("[^0-9]", "")) - 1;
            } catch (NumberFormatException ignored) {
            }
        }

        if (displayName.equalsIgnoreCase("Next Page")) {
            HomeCommand.openHomesGUI(player, currentPage + 1);
            return;
        } else if (displayName.equalsIgnoreCase("Previous Page")) {
            HomeCommand.openHomesGUI(player, currentPage - 1);
            return;
        }

        // Handle home teleport
        String homeName = displayName.toLowerCase();
        Location home = HomeManager.getHome(player.getUniqueId(), homeName);

        if (home != null) {
            player.closeInventory();
            player.teleport(home);
            player.sendMessage(getPrefix() + ChatColor.GREEN + "Teleported to home '" + homeName + "'.");
        } else {
            player.sendMessage(getPrefix() + ChatColor.RED + "That home isn't set.");
        }
    }
}
