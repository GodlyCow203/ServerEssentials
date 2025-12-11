package net.lunark.io.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.ChatColor;

public class JoinLeaveListener implements Listener {

    private final FileConfiguration config;

    public JoinLeaveListener(FileConfiguration config) {
        this.config = config;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        String message = config.getString("messages.join", "&aWelcome &f%player% &ato the server!");
        event.setJoinMessage(formatMessage(message, event.getPlayer().getName()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        String message = config.getString("messages.leave", "&c%player% &fhas left the server.");
        event.setQuitMessage(formatMessage(message, event.getPlayer().getName()));
    }

    private String formatMessage(String message, String playerName) {
        return ChatColor.translateAlternateColorCodes('&', message.replace("%player%", playerName));
    }
}