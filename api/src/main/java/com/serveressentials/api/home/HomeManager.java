package com.serveressentials.api.home;

import java.util.UUID;

public interface HomeManager {

    HomeAPI getAPI();

    void registerListener(Object listener);

    void unregisterListener(Object listener);
}