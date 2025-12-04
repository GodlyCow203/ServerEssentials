package net.lunark.io.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import net.lunark.io.Managers.JoinLeaveManager;

public class JoinLeaveListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        String message = JoinLeaveManager.getConfig().getString("join-message", "&aWelcome &f%player% &ato the server!");
        event.setJoinMessage(message.replace("%player%", event.getPlayer().getName()).replace("&", "§"));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        String message = JoinLeaveManager.getConfig().getString("leave-message", "&c%player% &fhas left the server.");
        event.setQuitMessage(message.replace("%player%", event.getPlayer().getName()).replace("&", "§"));
    }
}
