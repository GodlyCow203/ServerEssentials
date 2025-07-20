package serveressentials.serveressentials;


import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HexColorChatListener implements Listener {

    // Match <#RRGGBB>
    private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();

        // Replace <#RRGGBB> with ChatColor
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            String color = ChatColor.of("#" + hex).toString();
            matcher.appendReplacement(buffer, color);
        }
        matcher.appendTail(buffer);

        // Convert &x to section color codes (§x)
        String formatted = ChatColor.translateAlternateColorCodes('&', buffer.toString());

        event.setMessage(formatted);
    }
}
