package net.godlycow.org.api;

import com.serveressentials.api.PluginAPI;
import com.serveressentials.api.afk.AFKAPI;
import com.serveressentials.api.auction.AuctionAPI;
import com.serveressentials.api.back.BackAPI;
import com.serveressentials.api.daily.DailyAPI;
import com.serveressentials.api.economy.EconomyAPI;
import com.serveressentials.api.home.HomeAPI;
import com.serveressentials.api.shop.ShopAPI;
import net.godlycow.org.ServerEssentials;
import net.godlycow.org.afk.AFKManager;
import net.godlycow.org.afk.api.AFKAPIImpl;
import net.godlycow.org.auction.api.AuctionAPIImpl;
import net.godlycow.org.homes.HomeManager;

public class PluginAPIImpl implements PluginAPI {
    private final ServerEssentials plugin;
    private final ShopAPI shopAPI;
    private final HomeAPI homeAPI;
    private final AuctionAPI auctionAPI;
    private final AFKAPI afkAPI;
    private final BackAPI backAPI;
    private final DailyAPI dailyAPI;
    private final EconomyAPI economyAPI;




    public PluginAPIImpl(ServerEssentials plugin, ShopAPI shopAPI,
                         HomeManager homeManager, AuctionAPI auctionAPI,
                         AFKManager afkManager, BackAPI backAPI, DailyAPI dailyAPI, EconomyAPI economyAPI) {
        this.plugin = plugin;
        this.shopAPI = shopAPI;
        this.homeAPI = new net.godlycow.org.homes.api.HomeAPIImpl(homeManager);
        this.auctionAPI = auctionAPI;
        this.afkAPI = new AFKAPIImpl(plugin, afkManager);
        this.backAPI = backAPI;
        this.dailyAPI = dailyAPI;
        this.economyAPI = economyAPI;


    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean isEnabled() {
        return plugin.isEnabled();
    }

    @Override
    public ShopAPI getShopAPI() {
        return shopAPI;
    }

    @Override
    public HomeAPI getHomeAPI() {
        return homeAPI;
    }
    public DailyAPI getDailyAPI() {
        return dailyAPI;
    }

    public EconomyAPI getEconomyAPI() {
        return economyAPI;
    }

    @Override
    public AuctionAPI getAuctionAPI() {
        return auctionAPI;
    }

    @Override
    public AFKAPI getAFKAPI() {
        return afkAPI;
    }

    public BackAPI getBackAPI() {
        return backAPI;
    }
}