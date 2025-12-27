package com.serveressentials.api;

import com.serveressentials.api.home.HomeAPI;

public interface PluginAPI {
    HomeAPI getHomeAPI();
    String getVersion();
}