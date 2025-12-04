package net.lunark.io.scoreboard.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;


public class ColorUtil {

    private static final MiniMessage mini = MiniMessage.miniMessage();


    public static Component format(String input) {
        if (input == null) return Component.empty();
        return mini.deserialize(input);
    }



}
