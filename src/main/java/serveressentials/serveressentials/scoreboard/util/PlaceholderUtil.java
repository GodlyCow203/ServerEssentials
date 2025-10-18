package serveressentials.serveressentials.scoreboard.util;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class PlaceholderUtil {

    /**
     * Apply PlaceholderAPI placeholders safely.
     * Returns the processed string.
     */
    public static String apply(Player player, String text) {
        if (text == null) return "";
        if (player == null) return text;
        return PlaceholderAPI.setPlaceholders(player, text);
    }
}
