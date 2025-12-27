package com.serveressentials.api.home;

import org.bukkit.Location;

public interface Home {

    String getName();

    void setName(String name);

    HomeLocation getLocation();

    void setLocation(HomeLocation location);

    HomeMeta getMeta();

    void setMeta(HomeMeta meta);
}