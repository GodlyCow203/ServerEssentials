package net.godlycow.org.api;

import com.serveressentials.api.PluginAPI;
import com.serveressentials.api.home.HomeAPI;
import com.serveressentials.api.shop.ShopAPI;
import net.godlycow.org.ServerEssentials;
import net.godlycow.org.homes.HomeManager;

public class PluginAPIImpl implements PluginAPI {
    private final ServerEssentials plugin;
    private final ShopAPI shopAPI;
    private final HomeAPI homeAPI;

    public PluginAPIImpl(ServerEssentials plugin, ShopAPI shopAPI, HomeManager homeManager) {
        this.plugin = plugin;
        this.shopAPI = shopAPI;
        this.homeAPI = new net.godlycow.org.homes.api.HomeAPIImpl(homeManager);
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
}