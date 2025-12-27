package net.godlycow.org.api;

import com.serveressentials.api.PluginAPI;
import com.serveressentials.api.home.HomeAPI;

public class PluginAPIImpl implements PluginAPI {

    private final HomeAPI homeAPI;

    public PluginAPIImpl(HomeAPI homeAPI) {
        this.homeAPI = homeAPI;
    }

    @Override
    public HomeAPI getHomeAPI() {
        return homeAPI;
    }

    @Override
    public String getVersion() {
        return "2.0.7.9";
    }
}