package net.lunark.io.listeners;


import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import net.lunark.io.Managers.ConsoleCommandManager;

public class JoinCommandListener implements Listener {

    private final ConsoleCommandManager commandManager;

    public JoinCommandListener(ConsoleCommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPlayedBefore()) {
            commandManager.runCommands("first-join", event.getPlayer());
        }
        commandManager.runCommands("every-join", event.getPlayer());
    }
}
