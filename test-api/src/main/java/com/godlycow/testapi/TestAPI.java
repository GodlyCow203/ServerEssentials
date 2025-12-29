package com.godlycow.testapi;

import com.godlycow.testapi.BackAPITestCommand;
import com.serveressentials.api.PluginAPI;
import com.serveressentials.api.back.BackAPI;
import com.serveressentials.api.back.event.BackLocationSaveEvent;
import com.serveressentials.api.back.event.BackTeleportEvent;
import com.serveressentials.api.daily.DailyAPI;
import com.serveressentials.api.economy.EconomyAPI;
import com.serveressentials.api.shop.ShopAPI;
import com.serveressentials.api.auction.AuctionAPI;
import com.serveressentials.api.shop.event.ShopPurchaseEvent;
import com.serveressentials.api.shop.event.ShopSellEvent;
import com.serveressentials.api.afk.event.AFKStatusEvent;
import com.serveressentials.api.auction.event.AuctionListEvent;
import com.serveressentials.api.auction.event.AuctionPurchaseEvent;
import com.serveressentials.api.auction.event.AuctionRemoveEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class TestAPI extends JavaPlugin implements Listener {

    private static final String PREFIX = ChatColor.GOLD + "[ServerEssentialsTester] " + ChatColor.RESET;
    private PluginAPI pluginAPI;
    private ShopAPI shopAPI;
    private AuctionAPI auctionAPI;
    private BackAPI backAPI;
    private BackAPITestCommand backTestCommand;
    private int retryTaskId = -1;
    private com.serveressentials.api.daily.DailyAPI dailyAPI;
    private DailyAPITestCommand dailyTestCommand;
    private EconomyAPI economyAPI;
    private EconomyAPITestCommand economyTestCommand;


    @Override
    public void onEnable() {
        getLogger().info(PREFIX + "ServerEssentials API Tester enabled!");

        getCommand("shoptest").setExecutor(new ShopAPICommand(this));
        getCommand("auctiontest").setExecutor(new AuctionAPITestCommand(this));

        backTestCommand = new BackAPITestCommand(this);
        getCommand("backapitest").setExecutor(backTestCommand);

        dailyTestCommand = new DailyAPITestCommand(this);
        getCommand("dailyapitest").setExecutor(dailyTestCommand);

        economyTestCommand = new EconomyAPITestCommand(this);
        getCommand("economytest").setExecutor(economyTestCommand);

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
                    backAPI = pluginAPI.getBackAPI();

                    this.dailyAPI = pluginAPI.getDailyAPI();
                    if (dailyTestCommand != null) {
                        dailyTestCommand.setDailyAPI(dailyAPI);


                    }

                    this.economyAPI = pluginAPI.getEconomyAPI();
                    if (economyTestCommand != null) {
                        economyTestCommand.setEconomyAPI(economyAPI);
                    }

                    getLogger().info(PREFIX + "APIs connected successfully!");
                    getServer().getScheduler().cancelTask(retryTaskId);
                    retryTaskId = -1;

                    if (backTestCommand != null) {
                        backTestCommand.setBackAPI(backAPI);
                    }

                    testAPIFeatures();
                }
            }
        }, 100L, 100L).getTaskId();
    }

    private void testAPIFeatures() {
        if (shopAPI == null || auctionAPI == null || backAPI == null) return;

        getLogger().info(PREFIX + "Shop enabled: " + shopAPI.isShopEnabled());
        getLogger().info(PREFIX + "Auction enabled: " + auctionAPI.isAuctionEnabled());
        getLogger().info(PREFIX + "Back enabled: " + backAPI.isBackEnabled());
        getLogger().info(PREFIX + "Back enabled: " + dailyAPI.isDailyEnabled());
        getLogger().info(PREFIX + "Back enabled: " + economyAPI.isEnabled());

    }

    @EventHandler
    public void onBackTeleport(BackTeleportEvent event) {
        getLogger().info(PREFIX + "BackTeleport - Player: " + event.getPlayer().getName() +
                ", Type: " + event.getBackType() +
                ", From: " + formatLocation(event.getFrom()) +
                ", To: " + formatLocation(event.getTo()));
    }

    @EventHandler
    public void onBackLocationSave(BackLocationSaveEvent event) {
        getLogger().info(PREFIX + "BackLocationSave - Player: " + event.getPlayer().getName() +
                ", Location: " + formatLocation(event.getSavedLocation()));
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

    public BackAPI getBackAPI() {
        return backAPI;
    }

    private String formatLocation(Location loc) {
        return String.format("%.1f, %.1f, %.1f in %s", loc.getX(), loc.getY(), loc.getZ(), loc.getWorld().getName());
    }

    public EconomyAPI getEconomyAPI() {
        return economyAPI;
    }

    public DailyAPI getDailyAPI() {
        return dailyAPI;
    }
}