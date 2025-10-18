package serveressentials.serveressentials.scoreboard.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

/**
 * Utility to handle color conversions.
 * Uses Kyori MiniMessage for full RGB + legacy support.
 */
public class ColorUtil {

    private static final MiniMessage mini = MiniMessage.miniMessage();

    /**
     * Converts MiniMessage string into Adventure Component.
     */
    public static Component format(String input) {
        if (input == null) return Component.empty();
        return mini.deserialize(input);
    }



}
