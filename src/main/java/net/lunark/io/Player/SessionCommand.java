package net.lunark.io.Player;

import net.kyori.adventure.text.Component;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import net.lunark.io.util.PlayerMessages;
import net.lunark.io.Managers.SessionManager;

public class SessionCommand implements CommandExecutor {

    private final PlayerMessages messages;
    private final SessionManager sessionManager;

    public SessionCommand(PlayerMessages messages, SessionManager sessionManager) {
        this.messages = messages;
        this.sessionManager = sessionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("session.only-players"));
            return true;
        }

        long current = sessionManager.getCurrentSession(player);
        if (current == 0) {
            sender.sendMessage(messages.get("session.not-tracked"));
            return true;
        }

        long longest = sessionManager.getLongestSession(player);

        String currentStr = formatDuration(current);
        String longestStr = formatDuration(longest);

        Component msg = messages.get("session.info",
                "<current>", currentStr,
                "<longest>", longestStr);

        player.sendMessage(msg);
        return true;
    }

    private String formatDuration(long millis) {
        long seconds = millis / 1000 % 60;
        long minutes = millis / (1000 * 60) % 60;
        long hours = millis / (1000 * 60 * 60);

        return String.format("%02dh %02dm %02ds", hours, minutes, seconds);
    }
}
