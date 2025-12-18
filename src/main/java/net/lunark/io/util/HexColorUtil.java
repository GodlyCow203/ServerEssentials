package net.lunark.io.util;

public final class HexColorUtil {

    private HexColorUtil() {}

    /**
     * Converts hex to Minecraft color codes - always uses hex format
     * No fallback to legacy colors
     */
    public static String hex(String hexCode) {
        if (hexCode == null) {
            throw new IllegalArgumentException("Hex color cannot be null");
        }

        String cleaned = hexCode.replace("#", "").trim();
        if (cleaned.length() != 6) {
            throw new IllegalArgumentException("Invalid hex color: '" + hexCode + "'. Must be 6 characters (RRGGBB).");
        }

        if (!cleaned.matches("[0-9A-Fa-f]{6}")) {
            throw new IllegalArgumentException("Invalid hex color: '" + hexCode + "'. Must contain only hex digits (0-9, A-F).");
        }

        StringBuilder sb = new StringBuilder("§x");
        for (char c : cleaned.toCharArray()) {
            sb.append("§").append(Character.toLowerCase(c));
        }
        return sb.toString();
    }

    public static String colorize(String hexCode, String text) {
        return hex(hexCode) + text;
    }

    // **PURE HEX COLORS ONLY** - No legacy fallbacks
    public static final String PRIMARY = "#A855F7";      // Vibrant Purple
    public static final String SECONDARY = "#D8B4FE";    // Lavender

    public static final String SUCCESS = "#00D97E";
    public static final String WARNING = "#F6C343";
    public static final String DANGER = "#E94B3C";
    public static final String INFO = "#36B5E6";

    public static final String WHITE = "#FFFFFF";
    public static final String LIGHT_GRAY = "#D3D3D3";
    public static final String GRAY = "#9B9B9B";
    public static final String DARK_GRAY = "#5C5C5C";
    public static final String BLACK = "#000000";

    // **Direct hex application** - No version checks
    public static String primary(String text) {
        return colorize(PRIMARY, text);
    }

    public static String secondary(String text) {
        return colorize(SECONDARY, text);
    }

    public static String success(String text) {
        return colorize(SUCCESS, text);
    }

    public static String warning(String text) {
        return colorize(WARNING, text);
    }

    public static String danger(String text) {
        return colorize(DANGER, text);
    }

    public static String info(String text) {
        return colorize(INFO, text);
    }
}