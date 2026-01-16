package com.godlycow.testapi;

import com.serveressentials.api.shop.ShopAPI;
import com.serveressentials.api.shop.ShopItem;
import com.serveressentials.api.shop.ShopLayout;
import com.serveressentials.api.shop.ShopSection;
import com.serveressentials.api.shop.event.ShopPurchaseEvent;
import com.serveressentials.api.shop.event.ShopSellEvent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.Collection;
import java.util.Map;

public class ShopAPICommand implements org.bukkit.command.CommandExecutor {

    private final TestAPI plugin;
    private static final String PREFIX = ChatColor.GOLD + "[ShopTester] " + ChatColor.RESET;

    public ShopAPICommand(TestAPI plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Only players can use this command!");
            return true;
        }

        ShopAPI shopAPI = plugin.getShopAPI();

        if (shopAPI == null) {
            player.sendMessage(PREFIX + ChatColor.RED + "Shop API not available yet! Wait a few seconds...");
            player.sendMessage(PREFIX + ChatColor.YELLOW + "The API is still initializing...");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "status":
                testShopStatus(player);
                break;
            case "sections":
                testListSections(player);
                break;
            case "open":
                testOpenShop(player);
                break;
            case "section":
                if (args.length < 2) {
                    player.sendMessage(PREFIX + ChatColor.RED + "Usage: /shoptest section <sectionName>");
                    return true;
                }
                testOpenSection(player, args[1]);
                break;
            case "info":
                if (args.length < 2) {
                    player.sendMessage(PREFIX + ChatColor.RED + "Usage: /shoptest info <sectionName>");
                    return true;
                }
                testSectionInfo(player, args[1]);
                break;
            case "reload":
                testReloadShop(player);
                break;
            case "buy":
                testBuyEvent(player);
                break;
            case "sell":
                testSellEvent(player);
                break;
            case "all":
                testAllFeatures(player);
                break;
            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void testShopStatus(Player player) {
        player.sendMessage(PREFIX + ChatColor.GREEN + "=== Shop API Status ===");
        player.sendMessage(PREFIX + ChatColor.AQUA + "Shop Enabled: " +
                (plugin.getShopAPI().isShopEnabled() ? ChatColor.GREEN + "YES" : ChatColor.RED + "NO"));

        if (plugin.getPluginAPI() != null) {
            player.sendMessage(PREFIX + ChatColor.AQUA + "Plugin Version: " + ChatColor.WHITE + plugin.getPluginAPI().getVersion());
            player.sendMessage(PREFIX + ChatColor.AQUA + "Plugin Status: " +
                    (plugin.getPluginAPI().isEnabled() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
        }

        int sectionCount = plugin.getShopAPI().getSectionNames().size();
        player.sendMessage(PREFIX + ChatColor.AQUA + "Total Sections: " + ChatColor.WHITE + sectionCount);
        player.sendMessage(PREFIX + ChatColor.GREEN + "Shop API is working!");
    }

    private void testListSections(Player player) {
        player.sendMessage(PREFIX + ChatColor.GREEN + "=== Available Shop Sections ===");

        Collection<String> sections = plugin.getShopAPI().getSectionNames();
        if (sections.isEmpty()) {
            player.sendMessage(PREFIX + ChatColor.YELLOW + "No sections found!");
            return;
        }

        for (String section : sections) {
            ShopSection shopSection = plugin.getShopAPI().getSection(section);
            if (shopSection != null) {
                player.sendMessage(PREFIX + ChatColor.AQUA + "• " + section +
                        ChatColor.GRAY + " - " + shopSection.getTitle() +
                        ChatColor.DARK_GRAY + " [" + shopSection.getItems().size() + " items]");
            } else {
                player.sendMessage(PREFIX + ChatColor.RED + "• " + section + " (failed to load)");
            }
        }
    }

    private void testOpenShop(Player player) {
        try {
            plugin.getShopAPI().openShop(player);
            player.sendMessage(PREFIX + ChatColor.GREEN + "Main shop opened!");
        } catch (Exception e) {
            player.sendMessage(PREFIX + ChatColor.RED + "Error opening shop: " + e.getMessage());
        }
    }

    private void testOpenSection(Player player, String sectionName) {
        try {
            plugin.getShopAPI().openShopSection(player, sectionName);
            player.sendMessage(PREFIX + ChatColor.GREEN + "Section '" + sectionName + "' opened!");
        } catch (Exception e) {
            player.sendMessage(PREFIX + ChatColor.RED + "Error opening section: " + e.getMessage());
        }
    }

    private void testSectionInfo(Player player, String sectionName) {
        ShopSection section = plugin.getShopAPI().getSection(sectionName);
        if (section == null) {
            player.sendMessage(PREFIX + ChatColor.RED + "Section '" + sectionName + "' not found!");
            return;
        }

        player.sendMessage(PREFIX + ChatColor.GOLD + "=== Section: " + sectionName + " ===");
        player.sendMessage(PREFIX + ChatColor.AQUA + "Title: " + section.getTitle());
        player.sendMessage(PREFIX + ChatColor.AQUA + "Size: " + section.getSize());
        player.sendMessage(PREFIX + ChatColor.AQUA + "Items: " + section.getItems().size());
        player.sendMessage(PREFIX + ChatColor.AQUA + "Layout Items: " + section.getLayout().size());
    }

    private void testReloadShop(Player player) {
        player.sendMessage(PREFIX + ChatColor.YELLOW + "Reloading shop...");
        plugin.getShopAPI().reloadShop().thenAccept(success -> {
            if (success) {
                player.sendMessage(PREFIX + ChatColor.GREEN + "Shop reloaded!");
            } else {
                player.sendMessage(PREFIX + ChatColor.RED + "Reload failed!");
            }
        });
    }

    private void testBuyEvent(Player player) {
        ShopPurchaseEvent event = new ShopPurchaseEvent(
                player,
                player.getInventory().getItemInMainHand(),
                100.0,
                1
        );
        plugin.getServer().getPluginManager().callEvent(event);
        player.sendMessage(PREFIX + ChatColor.GREEN + "Purchase event fired! Check console for details.");
    }

    private void testSellEvent(Player player) {
        ShopSellEvent event = new ShopSellEvent(
                player,
                player.getInventory().getItemInMainHand(),
                50.0,
                1
        );
        plugin.getServer().getPluginManager().callEvent(event);
        player.sendMessage(PREFIX + ChatColor.GREEN + "Sell event fired! Check console for details.");
    }

    private void testAllFeatures(Player player) {
        player.sendMessage(PREFIX + ChatColor.GOLD + "Running all Shop API tests...");


        testShopStatus(player);
        testListSections(player);
        testOpenShop(player);
        testBuyEvent(player);
        testSellEvent(player);

        player.sendMessage(PREFIX + ChatColor.GOLD + "🎉 All Shop API tests completed!");
    }

    private void sendHelp(Player player) {
        player.sendMessage(PREFIX + ChatColor.GOLD + "=== Shop API Tester Commands ===");
        player.sendMessage(PREFIX + ChatColor.AQUA + "/shoptest status" + ChatColor.GRAY + " - Check API status");
        player.sendMessage(PREFIX + ChatColor.AQUA + "/shoptest sections" + ChatColor.GRAY + " - List all sections");
        player.sendMessage(PREFIX + ChatColor.AQUA + "/shoptest open" + ChatColor.GRAY + " - Open main shop");
        player.sendMessage(PREFIX + ChatColor.AQUA + "/shoptest section <name>" + ChatColor.GRAY + " - Open specific section");
        player.sendMessage(PREFIX + ChatColor.AQUA + "/shoptest info <name>" + ChatColor.GRAY + " - Get section info");
        player.sendMessage(PREFIX + ChatColor.AQUA + "/shoptest reload" + ChatColor.GRAY + " - Reload shop");
        player.sendMessage(PREFIX + ChatColor.AQUA + "/shoptest buy" + ChatColor.GRAY + " - Test purchase event");
        player.sendMessage(PREFIX + ChatColor.AQUA + "/shoptest sell" + ChatColor.GRAY + " - Test sell event");
        player.sendMessage(PREFIX + ChatColor.AQUA + "/shoptest all" + ChatColor.GRAY + " - Run all tests");
    }
}