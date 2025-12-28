package net.godlycow.org.afk.api;

import com.serveressentials.api.afk.AFKAPI;
import com.serveressentials.api.afk.event.AFKStatusEvent;
import net.godlycow.org.afk.AFKManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AFKAPIImpl implements AFKAPI {
    private final AFKManager afkManager;
    private final net.godlycow.org.ServerEssentials plugin;

    public AFKAPIImpl(net.godlycow.org.ServerEssentials plugin, AFKManager afkManager) {
        this.plugin = plugin;
        this.afkManager = afkManager;
    }

    @Override
    public boolean isAFK(Player player) {
        return afkManager.isAFK(player);
    }

    @Override
    public CompletableFuture<Boolean> setAFK(Player player, boolean afk) {
        return CompletableFuture.supplyAsync(() -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                afkManager.setAFK(player, afk);
                AFKStatusEvent event = new AFKStatusEvent(player, afk);
                Bukkit.getPluginManager().callEvent(event);
            });
            return true;
        });
    }

    @Override
    public CompletableFuture<Boolean> toggleAFK(Player player) {
        return setAFK(player, !isAFK(player));
    }

    @Override
    public Collection<UUID> getAFKPlayers() {
        return afkManager.getAFKPlayers();
    }

    @Override
    public long getAFKTimeoutSeconds() {
        return afkManager.getAFKTimeoutSeconds();
    }

    @Override
    public boolean isAFKEnabled() {
        return afkManager.isAFKEnabled();
    }

    @Override
    public void updatePlayerActivity(Player player) {
        afkManager.updatePlayerActivity(player);
    }
}