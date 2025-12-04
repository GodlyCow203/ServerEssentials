package net.lunark.io.staff;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import net.lunark.io.ServerEssentials;
import net.lunark.io.util.MessagesManager;

public class AdminChatUtils {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    /**
     * Sends a formatted admin chat message to all online staff with permission.
     * Supports MiniMessage formatting and placeholders.
     */
    public static void sendAdminMessage(Player sender, String message) {
        MessagesManager messages = ServerEssentials.getInstance().getMessagesManager();

        // Get the admin chat format from config
        Component formatted = messages.getMessageComponent(
                "adminchat.format",
                "%player%", sender.getName(),
                "%message%", message
        );

        // Send to all players with permission
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("serveressentials.adminchat")) {
                online.sendMessage(formatted);
            }
        }

        // Send to console
        Bukkit.getConsoleSender().sendMessage(miniMessage.serialize(formatted));
    }
}
