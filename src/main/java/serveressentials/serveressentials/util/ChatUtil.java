package serveressentials.serveressentials.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    /**
     * Converts a string with hex (&#RRGGBB) and legacy color codes to a Bukkit/Spigot readable string.
     */
    public static String color(String message) {
        if (message == null || message.isEmpty()) return "";

        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hexCode = matcher.group(1);
            String replacement = net.md_5.bungee.api.ChatColor.of("#" + hexCode).toString();
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(buffer);
        return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    /**
     * Converts a string with hex and legacy color codes to a Component (used in GUI titles).
     */
    public static Component component(String message) {
        if (message == null || message.isEmpty()) return Component.empty();

        // Replace hex with Adventure-compatible syntax
        String parsed = message.replaceAll("&#([A-Fa-f0-9]{6})", "§x§$1".replaceAll("([A-Fa-f0-9])", "§$1"));
        parsed = net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', parsed);
        return LegacyComponentSerializer.legacySection().deserialize(parsed);
    }

    public static String success(String msg) {
        return color("&#1BFF00" + msg);
    }

    public static String error(String msg) {
        return color("&#FF0000" + msg);
    }
}
