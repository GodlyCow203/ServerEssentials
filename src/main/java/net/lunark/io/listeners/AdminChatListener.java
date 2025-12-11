package net.lunark.io.listeners;

import net.kyori.adventure.text.Component;
import net.lunark.io.commands.impl.AdminChatCommand;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class AdminChatListener implements Listener {
    private final PlayerLanguageManager langManager;

    public AdminChatListener(PlayerLanguageManager langManager) {
        this.langManager = langManager;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (AdminChatCommand.isInAdminChat(player.getUniqueId())) {
            event.setCancelled(true);
            Component formatted = langManager.getMessageFor(player, "commands.adminchat.format", "<dark_gray>[<red>AdminChat<dark_gray>] <yellow>{player} <gray>» <white>{message}",
                    net.lunark.io.language.LanguageManager.ComponentPlaceholder.of("{player}", player.getName()),
                    net.lunark.io.language.LanguageManager.ComponentPlaceholder.of("{message}", event.getMessage()));
            player.getServer().getOnlinePlayers().stream().filter(p -> p.hasPermission("serveressentials.command.adminchat")).forEach(p -> p.sendMessage(formatted));
            player.getServer().getConsoleSender().sendMessage(formatted);
        }
    }
}