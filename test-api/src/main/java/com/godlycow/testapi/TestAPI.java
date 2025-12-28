package com.godlycow.testapi;

import com.serveressentials.api.PluginAPI;
import com.serveressentials.api.shop.*;
import com.serveressentials.api.shop.event.ShopPurchaseEvent;
import com.serveressentials.api.shop.event.ShopSellEvent;
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
    private int retryTaskId = -1;

    @Override
    public void onEnable() {
        getLogger().info(PREFIX + "Shop API Tester enabled!");
        getCommand("shoptest").setExecutor(new ShopAPICommand(this));
        getServer().getPluginManager().registerEvents(this, this);
        startAPIRetryTask();
    }

    private void startAPIRetryTask() {
        retryTaskId = getServer().getScheduler().runTaskTimer(this, () -> {
            if (pluginAPI == null) {
                pluginAPI = getServer().getServicesManager().load(PluginAPI.class);
                if (pluginAPI != null) {
                    shopAPI = pluginAPI.getShopAPI();
                    getLogger().info(PREFIX + "Shop API connected successfully!");
                    getLogger().info(PREFIX + "Shop API Version: " + pluginAPI.getVersion());

                    getServer().getScheduler().cancelTask(retryTaskId);
                    retryTaskId = -1;

                    testAPIFeatures();
                }
            }
        }, 100L, 100L).getTaskId();

        getServer().getScheduler().runTaskLater(this, () -> {
            if (pluginAPI == null) {
                getLogger().warning(PREFIX + "Still waiting for ServerEssentials API...");
                getLogger().warning(PREFIX + "Make sure ServerEssentials is loaded and enabled!");
            }
        }, 200L);
    }

    private void testAPIFeatures() {
        if (shopAPI == null) return;

        getLogger().info(PREFIX + "Testing Shop API features...");

        getLogger().info(PREFIX + "Shop enabled: " + shopAPI.isShopEnabled());
        getLogger().info(PREFIX + "Available sections: " + shopAPI.getSectionNames().size());

        for (String section : shopAPI.getSectionNames()) {
            getLogger().info(PREFIX + "Section: " + section);
        }
    }

    @EventHandler
    public void onShopPurchase(ShopPurchaseEvent event) {
        getLogger().info(PREFIX + "Purchase Event - Player: " + event.getPlayer().getName() +
                ", Item: " + event.getItem().getType() +
                ", Price: $" + event.getPrice());
    }

    @EventHandler
    public void onShopSell(ShopSellEvent event) {
        getLogger().info(PREFIX + "Sell Event - Player: " + event.getPlayer().getName() +
                ", Item: " + event.getItem().getType() +
                ", Price: $" + event.getPrice());
    }

    public ShopAPI getShopAPI() {
        return shopAPI;
    }

    public PluginAPI getPluginAPI() {
        return pluginAPI;
    }
}