package net.godlycow.org.serverEssentials.core;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.godlycow.org.managers.plugin.ReloadManager;
import net.godlycow.org.EssC;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

public class ServerEssentialsCommand implements CommandExecutor, TabCompleter {

    private final Plugin plugin;
    private final Map<String, Consumer<CommandSender>> subCommands = new LinkedHashMap<>();

    private final String PREFIX = gradient("#FFCC33", "#FFF2AA", "ServerEssentials") + " §8» §r";

    public ServerEssentialsCommand(Plugin plugin) {
        this.plugin = plugin;

        subCommands.put("help", this::sendHelp);
        subCommands.put("info", this::showInfo);
        subCommands.put("version", this::showVersion);
        subCommands.put("reload", this::reloadPlugin);
        subCommands.put("discord", this::sendDiscord);
        subCommands.put("website", this::sendWebsite);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        Consumer<CommandSender> action = subCommands.get(sub);

        if (action == null) {
            sendMessage(sender, "#FF5555", "Unknown subcommand. Use /" + label + " help");
            return true;
        }

        action.accept(sender);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return subCommands.keySet().stream()
                    .filter(s -> s.startsWith(input))
                    .sorted()
                    .toList();
        }
        return Collections.emptyList();
    }



    private void sendHelp(CommandSender sender) {
        sendTitle(sender, "Command Overview");
        sendMessage(sender, "#FFCC33", "• /se info - Plugin details");
        sendMessage(sender, "#FFCC33", "• /se version - Check for updates");
        sendMessage(sender, "#FFCC33", "• /se reload - Reload configuration");
        sendMessage(sender, "#FFCC33", "• /se discord - Community Discord");
        sendMessage(sender, "#FFCC33", "• /se website - Official Website");
    }

    private void showInfo(CommandSender sender) {
        sendTitle(sender, "ServerEssentials");
        sendMessage(sender, "#FFF2AA", "A clean, modern all-in-one server utility suite.");
        sendMessage(sender, "#AAAAAA", "Modules include:");
        sendMessage(sender, "#AAAAAA", "• RTP / Homes / Warps");
        sendMessage(sender, "#AAAAAA", "• Auction House / Shops");
        sendMessage(sender, "#AAAAAA", "• Ranks / Scoreboards");
        sendMessage(sender, "#AAAAAA", "• Admin Tools / GUIs");
        sendMessage(sender, "#FFCC33", "Performance-driven. Minimalistic. Modern.");
    }

    private void sendDiscord(CommandSender sender) {
        sendTitle(sender, "Discord");
        sendMessage(sender, "#FFCC33", "Join us: " + color("#FFFFFF") + "https://discord.gg/WJdxucXE");
    }

    private void sendWebsite(CommandSender sender) {
        sendTitle(sender, "Website");
        sendMessage(sender, "#FFCC33", "Visit: " + color("#FFFFFF") + "https://net.godlycow.org/");
    }

    private void showVersion(CommandSender sender) {
        String currentVersion = plugin.getDescription().getVersion();
        sendTitle(sender, "Version Checker");

        sendMessage(sender, "#FFCC33", "Current Version: " + color("#FFFFFF") + currentVersion);
        sendMessage(sender, "#AAAAAA", "Checking for updates...");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String latest = getLatestVersionFromModrinth();
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (latest == null) {
                    sendMessage(sender, "#FF5555", "Failed to fetch latest version.");
                    return;
                }

                sendMessage(sender, "#FFCC33", "Latest Version: " + color("#FFFFFF") + latest);

                if (!latest.equalsIgnoreCase(currentVersion)) {
                    sendMessage(sender, "#FFAA33", "Update available!");
                } else {
                    sendMessage(sender, "#55FF55", "You are running the latest version.");
                }
            });
        });
    }



    private void reloadPlugin(CommandSender sender) {
        if (!(plugin instanceof EssC)) {
            sendMessage(sender, "#FF5555", "Reload failed: plugin instance mismatch.");
            return;
        }

        sendTitle(sender, "Reloading");

        try {
            ReloadManager.reloadAll(sender);
            sendMessage(sender, "#55FF55", "Reload complete.");
        } catch (Exception e) {
            sendMessage(sender, "#FF5555", "An error occurred during reload.");
            e.printStackTrace();
        }
    }



    private void sendTitle(CommandSender sender, String title) {
        sender.sendMessage("");
        sender.sendMessage(PREFIX + gradient("#FFCC33", "#FFF2AA", "— " + title + " —"));
        sender.sendMessage("");
    }

    private void sendMessage(CommandSender sender, String hexColor, String message) {
        sender.sendMessage(PREFIX + color(hexColor) + message);
    }

    private static String color(String hexColor) {
        hexColor = hexColor.replace("#", "");
        if (hexColor.length() != 6) return "§f";
        StringBuilder sb = new StringBuilder("§x");
        for (char c : hexColor.toCharArray()) sb.append("§").append(c);
        return sb.toString();
    }

    private static String gradient(String startHex, String endHex, String text) {
        startHex = startHex.replace("#", "");
        endHex = endHex.replace("#", "");
        if (startHex.length() != 6 || endHex.length() != 6) return text;

        int[] start = {
                Integer.parseInt(startHex.substring(0, 2), 16),
                Integer.parseInt(startHex.substring(2, 4), 16),
                Integer.parseInt(startHex.substring(4, 6), 16)
        };
        int[] end = {
                Integer.parseInt(endHex.substring(0, 2), 16),
                Integer.parseInt(endHex.substring(2, 4), 16),
                Integer.parseInt(endHex.substring(4, 6), 16)
        };

        StringBuilder gradient = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            double ratio = (double) i / Math.max(1, text.length() - 1);
            int r = (int) (start[0] + ratio * (end[0] - start[0]));
            int g = (int) (start[1] + ratio * (end[1] - start[1]));
            int b = (int) (start[2] + ratio * (end[2] - start[2]));
            String hex = String.format("%02x%02x%02x", r, g, b);

            gradient.append("§x");
            for (char c : hex.toCharArray()) gradient.append("§").append(c);
            gradient.append(text.charAt(i));
        }

        return gradient.toString();
    }

    private String getLatestVersionFromModrinth() {
        try {
            URL url = new URL("https://api.modrinth.com/v2/project/serveressentials/version");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(4000);

            if (conn.getResponseCode() != 200) return null;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String response = reader.lines().reduce("", (a, b) -> a + b);
                int idx = response.indexOf("\"version_number\":");
                if (idx != -1) {
                    int start = response.indexOf('"', idx + 17) + 1;
                    int end = response.indexOf('"', start);
                    return response.substring(start, end);
                }
            }
        } catch (Exception ignored) {}

        return null;
    }
}
