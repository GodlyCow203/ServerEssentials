package com.serveressentials.api.afk;

import org.bukkit.entity.Player;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface AFKAPI {
    boolean isAFK(Player player);
    CompletableFuture<Boolean> setAFK(Player player, boolean afk);
    CompletableFuture<Boolean> toggleAFK(Player player);
    Collection<UUID> getAFKPlayers();
    long getAFKTimeoutSeconds();
    boolean isAFKEnabled();
    void updatePlayerActivity(Player player);
}