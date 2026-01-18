package net.godlycow.org.listeners.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinLeaveListener implements Listener {

    private final FileConfiguration config;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public JoinLeaveListener(FileConfiguration config) {
        this.config = config;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        String message = config.getString(
                "messages.join",
                "<green>Welcome <white><player></white> to the server!"
        );

        event.joinMessage(formatMessage(message, event.getPlayer().getName()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        String message = config.getString(
                "messages.leave",
                "<red><player></red> <white>has left the server."
        );

        event.quitMessage(formatMessage(message, event.getPlayer().getName()));
    }

    private Component formatMessage(String message, String playerName) {
        return miniMessage.deserialize(
                message,
                Placeholder.unparsed("player", playerName)
        );
    }
}
