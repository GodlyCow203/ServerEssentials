package com.godlycow.testapi;

import com.serveressentials.api.auction.AuctionAPI;
import com.serveressentials.api.auction.AuctionItem;
import com.serveressentials.api.auction.event.AuctionListEvent;
import com.serveressentials.api.auction.event.AuctionPurchaseEvent;
import com.serveressentials.api.auction.event.AuctionRemoveEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.UUID;

public class AuctionAPITestCommand implements CommandExecutor, Listener {
    private final TestAPI plugin;
    private static final String PREFIX = ChatColor.GOLD + "[AuctionTester] " + ChatColor.RESET;

    public AuctionAPITestCommand(TestAPI plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Only players can use this command!");
            return true;
        }

        AuctionAPI auctionAPI = plugin.getAuctionAPI();
        if (auctionAPI == null) {
            player.sendMessage(PREFIX + ChatColor.RED + "Auction API not available!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "status":
                testStatus(player, auctionAPI);
                break;
            case "list":
                testListItems(player, auctionAPI);
                break;
            case "open":
                auctionAPI.openAuction(player);
                player.sendMessage(PREFIX + ChatColor.GREEN + "✅ Auction opened!");
                break;
            case "my":
                auctionAPI.openMyAuctionItems(player);
                player.sendMessage(PREFIX + ChatColor.GREEN + "✅ Your items GUI opened!");
                break;
            case "add":
                if (args.length < 2) {
                    player.sendMessage(PREFIX + ChatColor.RED + "Usage: /auctiontest add <price>");
                    return true;
                }
                testAddItem(player, auctionAPI, args[1]);
                break;
            case "remove":
                if (args.length < 2) {
                    player.sendMessage(PREFIX + ChatColor.RED + "Usage: /auctiontest remove <itemId>");
                    return true;
                }
                testRemoveItem(player, auctionAPI, args[1]);
                break;
            default:
                sendHelp(player);
                break;
        }
        return true;
    }

    private void testStatus(Player player, AuctionAPI api) {
        player.sendMessage(PREFIX + ChatColor.GREEN + "=== Auction API Status ===");
        player.sendMessage(PREFIX + ChatColor.AQUA + "Auction Enabled: " +
                (api.isAuctionEnabled() ? ChatColor.GREEN + "✅ YES" : ChatColor.RED + "❌ NO"));
        player.sendMessage(PREFIX + ChatColor.AQUA + "Max Price: " + ChatColor.YELLOW + api.getMaxPriceLimit());
        player.sendMessage(PREFIX + ChatColor.AQUA + "Max Items/Player: " + ChatColor.YELLOW + api.getMaxItemsPerPlayer());
        player.sendMessage(PREFIX + ChatColor.GREEN + "✅ Auction API is working!");
    }

    private void testListItems(Player player, AuctionAPI api) {
        player.sendMessage(PREFIX + ChatColor.GREEN + "=== Active Auction Items ===");
        api.getActiveItems().thenAccept(items -> {
            if (items.isEmpty()) {
                player.sendMessage(PREFIX + ChatColor.YELLOW + "No items in auction!");
                return;
            }
            items.forEach(item -> {
                player.sendMessage(PREFIX + ChatColor.AQUA + "• " +
                        item.getItem().getType() + " - $" + item.getPrice());
            });
        });
    }

    private void testAddItem(Player player, AuctionAPI api, String priceStr) {
        try {
            double price = Double.parseDouble(priceStr);
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand == null || hand.getType().isAir()) {
                player.sendMessage(PREFIX + ChatColor.RED + "You must hold an item!");
                return;
            }

            api.addAuctionItem(player, hand.clone(), price).thenAccept(success -> {
                if (success) {
                    player.sendMessage(PREFIX + ChatColor.GREEN + "✅ Item listed for auction!");
                    player.getInventory().setItemInMainHand(null);
                } else {
                    player.sendMessage(PREFIX + ChatColor.RED + "❌ Failed to list item!");
                }
            });
        } catch (NumberFormatException e) {
            player.sendMessage(PREFIX + ChatColor.RED + "Invalid price!");
        }
    }

    private void testRemoveItem(Player player, AuctionAPI api, String itemId) {
        try {
            api.removeAuctionItem(UUID.fromString(itemId)).thenAccept(success -> {
                player.sendMessage(success ?
                        PREFIX + ChatColor.GREEN + "✅ Item removed!" :
                        PREFIX + ChatColor.RED + "❌ Item not found or removal failed!"
                );
            });
        } catch (IllegalArgumentException e) {
            player.sendMessage(PREFIX + ChatColor.RED + "Invalid UUID!");
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage(PREFIX + ChatColor.GOLD + "=== Auction API Tester ===");
        player.sendMessage(PREFIX + ChatColor.AQUA + "/auctiontest status" + ChatColor.GRAY + " - Check API status");
        player.sendMessage(PREFIX + ChatColor.AQUA + "/auctiontest list" + ChatColor.GRAY + " - List all items");
        player.sendMessage(PREFIX + ChatColor.AQUA + "/auctiontest open" + ChatColor.GRAY + " - Open auction GUI");
        player.sendMessage(PREFIX + ChatColor.AQUA + "/auctiontest my" + ChatColor.GRAY + " - Open your items");
        player.sendMessage(PREFIX + ChatColor.AQUA + "/auctiontest add <price>" + ChatColor.GRAY + " - Sell held item");
        player.sendMessage(PREFIX + ChatColor.AQUA + "/auctiontest remove <itemId>" + ChatColor.GRAY + " - Remove item");
    }

    @EventHandler
    public void onAuctionList(AuctionListEvent event) {
        plugin.getLogger().info(PREFIX + "List Event - Player: " + event.getPlayer().getName() +
                ", Item: " + event.getItem().getType() + ", Price: $" + event.getPrice());
    }

    @EventHandler
    public void onAuctionPurchase(AuctionPurchaseEvent event) {
        plugin.getLogger().info(PREFIX + "Purchase Event - Buyer: " + event.getBuyer().getName() +
                ", Seller: " + event.getSellerId() + ", Item: " + event.getItem().getType());
    }

    @EventHandler
    public void onAuctionRemove(AuctionRemoveEvent event) {
        plugin.getLogger().info(PREFIX + "Remove Event - Player: " + event.getPlayer().getName() +
                ", Item: " + event.getItem().getType());
    }
}