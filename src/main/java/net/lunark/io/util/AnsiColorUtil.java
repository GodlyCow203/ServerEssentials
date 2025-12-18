package net.lunark.io.util;

public final class AnsiColorUtil {

    private static final String RESET = "\033[0m";
    private static final boolean ANSI_SUPPORTED = isAnsiSupported();

    private AnsiColorUtil() {}

    /**
     * Detects if ANSI colors are supported in the current console
     */
    private static boolean isAnsiSupported() {
        String os = System.getProperty("os.name").toLowerCase();
        // Windows 10+ supports ANSI, Unix/Linux/macOS always support it
        return System.console() != null ||
                os.contains("win") ||
                os.contains("nix") ||
                os.contains("nux") ||
                os.contains("mac");
    }

    /**
     * Converts hex to ANSI 24-bit truecolor escape sequence
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

        int r = Integer.parseInt(cleaned.substring(0, 2), 16);
        int g = Integer.parseInt(cleaned.substring(2, 4), 16);
        int b = Integer.parseInt(cleaned.substring(4, 6), 16);

        // ANSI truecolor format: \033[38;2;<r>;<g>;<b>m
        return String.format("\033[38;2;%d;%d;%dm", r, g, b);
    }

    /**
     * Applies color to text with automatic reset
     */
    public static String colorize(String hexCode, String text) {
        if (!ANSI_SUPPORTED) {
            return text; // Plain text if no ANSI support
        }
        return hex(hexCode) + text + RESET;
    }

    // **ANSI COLOR CONSTANTS**
    public static final String PRIMARY = "#A855F7";      // Vibrant Purple
    public static final String SECONDARY = "#D8B4FE";    // Lavender

    public static final String SUCCESS = "#00D97E";
    public static final String WARNING = "#F6C343";
    public static final String DANGER = "#E94B3C";
    public static final String INFO = "#36B5E6";

    public static final String WHITE = "#FFFFFF";
    public static final String GRAY = "#9B9B9B";

    // **Convenience methods**
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