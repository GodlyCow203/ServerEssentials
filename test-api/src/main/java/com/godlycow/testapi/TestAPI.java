package com.godlycow.testapi;

import com.godlycow.testapi.BackAPITestCommand;
import com.serveressentials.api.PluginAPI;
import com.serveressentials.api.back.BackAPI;
import com.serveressentials.api.back.event.BackLocationSaveEvent;
import com.serveressentials.api.back.event.BackTeleportEvent;
import com.serveressentials.api.daily.DailyAPI;
import com.serveressentials.api.economy.EconomyAPI;
import com.serveressentials.api.kit.KitAPI;
import com.serveressentials.api.lobby.LobbyAPI;
import com.serveressentials.api.mail.MailAPI;
import com.serveressentials.api.nick.NickAPI;
import com.serveressentials.api.report.ReportAPI;
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
    private DailyAPI dailyAPI;
    private DailyAPITestCommand dailyTestCommand;
    private EconomyAPI economyAPI;
    private EconomyAPITestCommand economyTestCommand;
    private KitAPI kitAPI;
    private KitAPITestCommand kitTestCommand;
    private LobbyAPI lobbyAPI;
    private LobbyAPITestCommand lobbyTestCommand;
    private MailAPI mailAPI;
    private MailAPITestCommand mailTestCommand;
    private NickAPI nickAPI;
    private NickAPITestCommand nickTestCommand;
    private ReportAPI reportAPI;
    private ReportAPITestCommand reportTestCommand;

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

        kitTestCommand = new KitAPITestCommand(this);
        getCommand("kittest").setExecutor(kitTestCommand);

        lobbyTestCommand = new LobbyAPITestCommand(this);
        getCommand("lobbyapitest").setExecutor(lobbyTestCommand);

        mailTestCommand = new MailAPITestCommand(this);
        getCommand("mailapitest").setExecutor(mailTestCommand);

        nickTestCommand = new NickAPITestCommand(this);
        getCommand("nickapitest").setExecutor(nickTestCommand);

        reportTestCommand = new ReportAPITestCommand(this);
        getCommand("reportapitest").setExecutor(reportTestCommand);

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
                    dailyAPI = pluginAPI.getDailyAPI();
                    economyAPI = pluginAPI.getEconomyAPI();
                    kitAPI = pluginAPI.getKitAPI();
                    lobbyAPI = pluginAPI.getLobbyAPI();
                    mailAPI = pluginAPI.getMailAPI();
                    nickAPI = pluginAPI.getNickAPI();
                    reportAPI = pluginAPI.getReportAPI();





                    if (dailyTestCommand != null) {
                        dailyTestCommand.setDailyAPI(dailyAPI);
                    }
                    if (economyTestCommand != null) {
                        economyTestCommand.setEconomyAPI(economyAPI);
                    }
                    if (kitTestCommand != null) {
                        kitTestCommand.setAPI(kitAPI);
                    }
                    if (backTestCommand != null) {
                        backTestCommand.setBackAPI(backAPI);
                    }
                    if (lobbyTestCommand != null) {
                        lobbyTestCommand.setAPI(lobbyAPI);
                    }

                    if (mailTestCommand != null) {
                        mailTestCommand.setAPI(mailAPI);
                    }

                    if (nickTestCommand != null) {
                        nickTestCommand.setAPI(nickAPI);
                    }

                    if (reportTestCommand != null) {
                        reportTestCommand.setAPI(reportAPI);
                    }

                    getLogger().info(PREFIX + "APIs connected successfully!");
                    getServer().getScheduler().cancelTask(retryTaskId);
                    retryTaskId = -1;

                    testAPIFeatures();
                }
            }
        }, 100L, 100L).getTaskId();
    }

    private void testAPIFeatures() {
        if (shopAPI == null || auctionAPI == null || backAPI == null || dailyAPI == null || economyAPI == null || kitAPI == null) {
            getLogger().warning(PREFIX + "One or more APIs are null, skipping test.");
            return;
        }

        getLogger().info(PREFIX + "Shop enabled: " + shopAPI.isShopEnabled());
        getLogger().info(PREFIX + "Auction enabled: " + auctionAPI.isAuctionEnabled());
        getLogger().info(PREFIX + "Back enabled: " + backAPI.isBackEnabled());
        getLogger().info(PREFIX + "Daily enabled: " + dailyAPI.isDailyEnabled());
        getLogger().info(PREFIX + "Economy enabled: " + economyAPI.isEnabled());
        getLogger().info(PREFIX + "Kit enabled: " + kitAPI.isEnabled());
        getLogger().info(PREFIX + "Lobby enabled: " + lobbyAPI.isEnabled());
        getLogger().info(PREFIX + "Mail enabled: " + mailAPI.isEnabled());
        getLogger().info(PREFIX + "Nick enabled: " + nickAPI.isEnabled());
        if (reportAPI == null) {
            getLogger().warning(PREFIX + "Report module not detected (disabled or not installed).");
        }




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
    public NickAPI getNickAPI() {
        return nickAPI;
    }

    public PluginAPI getPluginAPI() {
        return pluginAPI;
    }

    public AuctionAPI getAuctionAPI() {
        return auctionAPI;
    }

    public KitAPI getKitAPI() {
        return kitAPI;
    }
    public LobbyAPI getLobbyAPI() {
        return lobbyAPI;
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
    public ReportAPI getReportAPI() {
        return reportAPI;
    }

    public MailAPI getMailAPI() {
        return mailAPI;
    }
}