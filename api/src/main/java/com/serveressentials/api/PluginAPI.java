package com.serveressentials.api;

import com.serveressentials.api.home.HomeAPI;
import com.serveressentials.api.shop.ShopAPI;

/**
 * Main plugin API interface
 */
public interface PluginAPI {
    String getVersion();
    boolean isEnabled();
    ShopAPI getShopAPI();
    HomeAPI getHomeAPI();
}