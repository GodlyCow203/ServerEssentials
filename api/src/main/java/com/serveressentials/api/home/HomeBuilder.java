package com.serveressentials.api.home;

import org.bukkit.Location;

public interface HomeBuilder {

    HomeBuilder name(String name);

    HomeBuilder location(Location location);

    HomeBuilder creator(String creatorName);

    HomeBuilder isPublic(boolean isPublic);

    Home build();
}