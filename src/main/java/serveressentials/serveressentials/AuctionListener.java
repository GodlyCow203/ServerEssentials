package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class AuctionListener implements Listener {

    private final AuctionManager manager;

    public AuctionListener(AuctionManager manager) {
        this.manager = manager;
    }

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @EventHandler
    public void onAuctionClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Sicherstellen, dass Klick im Inventar des Auction GUI war (und nicht z.B. im eigenen Inventar)
        if (event.getClickedInventory() == null) return;

        String title = ChatColor.stripColor(event.getView().getTitle());

        if (!title.startsWith("Auction House")) return;
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) return;

        String name = meta.hasDisplayName() ? ChatColor.stripColor(meta.getDisplayName()) : "";

        int page = 0;
        if (title.contains("Page")) {
            try {
                page = Integer.parseInt(title.replaceAll("[^0-9]", "")) - 1;
                if (page < 0) page = 0;
            } catch (NumberFormatException ignored) {}
        }

        if (name.equalsIgnoreCase("Previous Page")) {
            new AuctionGUI(manager).open(player, page - 1 < 0 ? 0 : page - 1);
            return;
        } else if (name.equalsIgnoreCase("Next Page")) {
            new AuctionGUI(manager).open(player, page + 1);
            return;
        } else if (name.equalsIgnoreCase("Close")) {
            player.closeInventory();
            return;
        }

        NamespacedKey idKey = new NamespacedKey(ServerEssentials.getInstance(), "auction-id");
        Integer auctionId = meta.getPersistentDataContainer().get(idKey, PersistentDataType.INTEGER);

        if (auctionId == null) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Could not identify this item.");
            return;
        }

        AuctionItem auctionItem = manager.getItemById(auctionId);

        if (auctionItem == null) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Item no longer available.");
            return;
        }

        if (auctionItem.getSeller().equals(player.getUniqueId())) {
            player.sendMessage(getPrefix() + ChatColor.YELLOW + "You can't buy your own item.");
            return;
        }

        double price = auctionItem.getPrice();
        double balance = EconomyManager.getBalance(player.getUniqueId());

        if (balance < price) {
            player.sendMessage(getPrefix() + ChatColor.RED + "You don't have enough money.");
            return;
        }

        // Check for free inventory slot
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Your inventory is full.");
            return;
        }

        EconomyManager.takeBalance(player.getUniqueId(), price);
        player.getInventory().addItem(auctionItem.getItem());
        manager.removeItem(auctionItem);

        player.sendMessage(getPrefix() + ChatColor.GREEN + "You bought the item for $" + String.format("%.2f", price) + "!");

        final int finalPage = page;
        Bukkit.getScheduler().runTaskLater(ServerEssentials.getInstance(), () -> {
            new AuctionGUI(manager).open(player, finalPage);
        }, 2L);
    }
}
