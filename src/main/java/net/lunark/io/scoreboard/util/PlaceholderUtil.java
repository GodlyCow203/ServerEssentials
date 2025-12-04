package net.lunark.io.scoreboard.util;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class PlaceholderUtil {


    public static String apply(Player player, String text) {
        if (text == null) return "";
        if (player == null) return text;
        return PlaceholderAPI.setPlaceholders(player, text);
    }
}
