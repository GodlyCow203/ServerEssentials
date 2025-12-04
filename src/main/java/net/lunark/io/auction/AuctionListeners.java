package net.lunark.io.auction;

import net.lunark.io.ServerEssentials;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class AuctionListeners implements Listener {

    private final ServerEssentials plugin;

    public AuctionListeners(ServerEssentials plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        String title = e.getView().getTitle();
        if (!title.contains("Auction House") && !title.contains("Confirm Remove") && !title.contains("Your Items")) return;

        e.setCancelled(true);
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (title.contains("Confirm Remove")) {
            AuctionItem itemToRemove = plugin.getGuiManager().getItemToRemove(player);
            if (clicked.getType() == Material.GREEN_WOOL) {
                if (itemToRemove != null) {
                    player.getInventory().addItem(itemToRemove.getItem().clone());
                    plugin.getAuctionManager().removeAuctionItem(itemToRemove);

                    player.sendMessage(plugin.getAuctionMessagesManager()
                            .getMessage("remove.success"));
                } else {
                    player.sendMessage(plugin.getAuctionMessagesManager()
                            .getMessage("remove.not-found"));
                }
                plugin.getGuiManager().openPlayerItemsGUI(player, 1);
            } else if (clicked.getType() == Material.RED_WOOL) {
                player.sendMessage(plugin.getAuctionMessagesManager()
                        .getMessage("remove.cancel"));
                plugin.getGuiManager().openPlayerItemsGUI(player, 1);
            }
            return;
        }

        switch (clicked.getType()) {
            case BARRIER -> player.closeInventory();
            case ARROW -> {
                int page = extractPage(title);
                if (clicked.getItemMeta().getDisplayName().contains("Next")) {
                    if (title.contains("Auction House"))
                        plugin.getGuiManager().openAuctionGUI(player, page + 1);
                    else
                        plugin.getGuiManager().openPlayerItemsGUI(player, page + 1);
                } else {
                    if (title.contains("Auction House"))
                        plugin.getGuiManager().openAuctionGUI(player, Math.max(page - 1, 1));
                    else
                        plugin.getGuiManager().openPlayerItemsGUI(player, Math.max(page - 1, 1));
                }
            }
            case PAPER -> {
                if (title.contains("Auction House")) {
                    plugin.getGuiManager().openAuctionGUI(player, extractPage(title));
                    player.sendMessage(plugin.getAuctionMessagesManager()
                            .getMessage("auction.refresh"));
                }
            }
            case CHEST -> {
                if (title.contains("Auction House")) {
                    plugin.getGuiManager().openPlayerItemsGUI(player, 1);
                }
            }
            case PLAYER_HEAD -> {
                if (title.contains("Auction House")) {
                    double balance = plugin.getVaultEconomy().getBalance(player);
                    player.sendMessage(plugin.getAuctionMessagesManager()
                            .getMessage("auction.balance", "%balance%", String.valueOf(balance)));
                }
            }
        }

        if (title.contains("Your Items")) {
            AuctionItem auctionItem = plugin.getGuiManager().getPlayerItem(player, e.getRawSlot());
            if (auctionItem != null) plugin.getGuiManager().openRemoveConfirmGUI(player, auctionItem);
            return;
        }

        if (title.contains("Auction House")) {
            AuctionItem auctionItem = plugin.getGuiManager().getAuctionItem(player, e.getRawSlot());
            if (auctionItem == null) return;

            double price = auctionItem.getPrice();
            if (plugin.getVaultEconomy().getBalance(player) < price) {
                player.sendMessage(plugin.getAuctionMessagesManager()
                        .getMessage("auction.not-enough-money"));
                return;
            }

            plugin.getVaultEconomy().withdrawPlayer(player, price);

            UUID sellerUUID = auctionItem.getSeller();
            if (Bukkit.getOfflinePlayer(sellerUUID).isOnline()) {
                Player seller = Bukkit.getPlayer(sellerUUID);
                plugin.getVaultEconomy().depositPlayer(seller, price);
                seller.sendMessage(plugin.getAuctionMessagesManager()
                        .getMessage("auction.sold",
                                "%player%", player.getName(),
                                "%price%", String.valueOf(price)));
            } else {
                plugin.getVaultEconomy().depositPlayer(Bukkit.getOfflinePlayer(sellerUUID), price);
            }

            player.getInventory().addItem(auctionItem.getItem().clone());
            player.sendMessage(plugin.getAuctionMessagesManager()
                    .getMessage("auction.purchase", "%price%", String.valueOf(price)));

            plugin.getAuctionManager().removeAuctionItem(auctionItem);
            plugin.getGuiManager().openAuctionGUI(player, extractPage(title));
        }
    }

    private int extractPage(String title) {
        if (!title.matches(".*Page \\d+.*")) return 1;
        return Integer.parseInt(title.replaceAll(".*Page (\\d+).*", "$1"));
    }
}
