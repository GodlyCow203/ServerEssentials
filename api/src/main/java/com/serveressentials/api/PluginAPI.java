package com.serveressentials.api;

import com.serveressentials.api.afk.AFKAPI;
import com.serveressentials.api.auction.AuctionAPI;
import com.serveressentials.api.back.BackAPI;
import com.serveressentials.api.daily.DailyAPI;
import com.serveressentials.api.economy.EconomyAPI;
import com.serveressentials.api.home.HomeAPI;
import com.serveressentials.api.kit.KitAPI;
import com.serveressentials.api.shop.ShopAPI;


public interface PluginAPI {
    String getVersion();
    boolean isEnabled();
    ShopAPI getShopAPI();
    HomeAPI getHomeAPI();
    AuctionAPI getAuctionAPI();
    AFKAPI getAFKAPI();
    BackAPI getBackAPI();
    DailyAPI getDailyAPI();
    EconomyAPI getEconomyAPI();
    KitAPI getKitAPI();
}