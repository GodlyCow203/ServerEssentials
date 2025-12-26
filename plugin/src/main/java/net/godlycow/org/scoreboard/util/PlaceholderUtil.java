package net.godlycow.org.scoreboard.util;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PlaceholderUtil {
    private static final Pattern BUILTIN_PATTERN = Pattern.compile("\\{([^}]+)\\}");

    private PlaceholderUtil() {}

    public static String apply(Player player, String text) {
        if (text == null) return "";
        if (player == null) return text;

        String result = text;
        Matcher matcher = BUILTIN_PATTERN.matcher(result);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String value = getBuiltinPlaceholder(player, placeholder);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(sb);
        result = sb.toString();

        try {
            return PlaceholderAPI.setPlaceholders(player, result);
        } catch (NoClassDefFoundError e) {
            return result;
        } catch (Exception e) {
            return result;
        }
    }

    private static String getBuiltinPlaceholder(Player player, String placeholder) {
        return switch (placeholder.toLowerCase()) {
            case "player_name" -> player.getName();
            case "player_uuid" -> player.getUniqueId().toString();
            case "world" -> player.getWorld().getName();
            case "online_players" -> String.valueOf(Bukkit.getOnlinePlayers().size());
            case "max_players" -> String.valueOf(Bukkit.getMaxPlayers());
            case "tps" -> String.format("%.1f", Bukkit.getTPS()[0]);
            case "ping" -> {
                try {
                    yield String.valueOf(player.getPing());
                } catch (NoSuchMethodError e) {
                    yield "N/A";
                }
            }
            default -> "{" + placeholder + "}";
        };
    }
}