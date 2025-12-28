package com.godlycow.testapi;

import com.serveressentials.api.PluginAPI;
import com.serveressentials.api.shop.ShopAPI;
import com.serveressentials.api.auction.AuctionAPI;
import com.serveressentials.api.shop.event.ShopPurchaseEvent;
import com.serveressentials.api.shop.event.ShopSellEvent;
import com.serveressentials.api.auction.event.AuctionListEvent;
import com.serveressentials.api.auction.event.AuctionPurchaseEvent;
import com.serveressentials.api.auction.event.AuctionRemoveEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class TestAPI extends JavaPlugin implements Listener {

    private static final String PREFIX = ChatColor.GOLD + "[ShopTester] " + ChatColor.RESET;
    private PluginAPI pluginAPI;
    private ShopAPI shopAPI;
    private AuctionAPI auctionAPI;
    private int retryTaskId = -1;


    @Override
    public void onEnable() {
        getLogger().info(PREFIX + "ServerEssentials API Tester enabled!");

        getCommand("shoptest").setExecutor(new ShopAPICommand(this));
        getCommand("afktest").setExecutor(new AFKAPITestCommand(this));
        getCommand("auctiontest").setExecutor(new AuctionAPITestCommand(this));

        getServer().getPluginManager().registerEvents(this, this);

        startAPIRetryTask();
    }

    private void startAPIRetryTask() {
        retryTaskId = getServer().getScheduler().runTaskTimer(this, () -> {
            if (pluginAPI == null) {
                pluginAPI = getServer().getServicesManager().load(PluginAPI.class);
                if (pluginAPI != null) {
                    shopAPI = pluginAPI.getShopAPI();
                    auctionAPI = pluginAPI.getAuctionAPI();
                    getLogger().info(PREFIX + "APIs connected successfully!");
                    getServer().getScheduler().cancelTask(retryTaskId);
                    retryTaskId = -1;
                    testAPIFeatures();
                }
            }
        }, 100L, 100L).getTaskId();
    }

    private void testAPIFeatures() {
        if (shopAPI == null || auctionAPI == null) return;

        getLogger().info(PREFIX + "Shop enabled: " + shopAPI.isShopEnabled());
        getLogger().info(PREFIX + "Auction enabled: " + auctionAPI.isAuctionEnabled());
        getLogger().info(PREFIX + "Shop sections: " + shopAPI.getSectionNames().size());
        getLogger().info(PREFIX + "Auction max price: " + auctionAPI.getMaxPriceLimit());
    }

    @EventHandler
    public void onShopPurchase(ShopPurchaseEvent event) {
        getLogger().info(PREFIX + "Purchase - Player: " + event.getPlayer().getName() +
                ", Item: " + event.getItem().getType() + ", Price: $" + event.getPrice());
    }

    @EventHandler
    public void onShopSell(ShopSellEvent event) {
        getLogger().info(PREFIX + "Sell - Player: " + event.getPlayer().getName() +
                ", Item: " + event.getItem().getType() + ", Price: $" + event.getPrice());
    }

    public ShopAPI getShopAPI() {
        return shopAPI;
    }

    public PluginAPI getPluginAPI() {
        return pluginAPI;
    }

    public AuctionAPI getAuctionAPI() {
        return auctionAPI;
    }
}