package net.lunark.io.serverEssentials;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.json.JSONArray;
import org.json.JSONObject;
import net.lunark.io.ServerEssentials;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

// Adventure (MiniMessage)
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class VersionChecker {

    private static final String MODRINTH_PROJECT_ID = "K7HZMVgx";
    private static String latestVersion = "unknown";

    // MiniMessage + Legacy for console
    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.builder().hexColors().useUnusualXRepeatedCharacterHexFormat().build();

    /** Convert MiniMessage → legacy hex format for console output (strip gradients) */
    private static String mmForConsole(String mini) {
        var component = MINI.deserialize(mini);
        String legacy = LEGACY.serialize(component);

        // Manually replace gradient hex sequences
        // §x§R§R§G§G§B§B → keep as §#RRGGBB
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < legacy.length(); i++) {
            if (i + 13 < legacy.length() && legacy.charAt(i) == '§' && legacy.charAt(i + 1) == 'x') {
                String hex = "" + legacy.charAt(i + 2) + legacy.charAt(i + 3)
                        + legacy.charAt(i + 4) + legacy.charAt(i + 5)
                        + legacy.charAt(i + 6) + legacy.charAt(i + 7)
                        + legacy.charAt(i + 8) + legacy.charAt(i + 9)
                        + legacy.charAt(i + 10) + legacy.charAt(i + 11)
                        + legacy.charAt(i + 12) + legacy.charAt(i + 13);
                sb.append("§#").append(hex.substring(0, 6)); // keep first 6 digits
                i += 13; // skip processed chars
            } else {
                sb.append(legacy.charAt(i));
            }
        }

        return sb.toString();
    }


    /**
     * Fetch the latest version from Modrinth
     */
    public static void checkLatestVersion(Plugin plugin) {
        HttpClient client = HttpClient.newHttpClient();
        String url = "https://api.modrinth.com/v2/project/" + MODRINTH_PROJECT_ID + "/version";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    try {
                        JSONArray versions = new JSONArray(response);
                        if (versions.length() > 0) {
                            JSONObject latest = versions.getJSONObject(0);
                            latestVersion = latest.getString("version_number");

                            String currentVersion = plugin.getDescription().getVersion();

                            Bukkit.getLogger().info("");
                            Bukkit.getLogger().info("§8================================");

                            if (!currentVersion.equalsIgnoreCase(latestVersion)) {

                                Bukkit.getLogger().info(mmForConsole("<gradient:#FFCC33:#FFF2AA>ServerEssentials - Update Available</gradient>"));
                                Bukkit.getLogger().info("§8--------------------------------");

                                Bukkit.getLogger().info(mmForConsole("<gray>Current: </gray><#E84200>" + currentVersion + "</#E84200>"));
                                Bukkit.getLogger().info(mmForConsole("<gray>Latest: </gray><#AAE800>" + latestVersion + "</#AAE800>"));
                                Bukkit.getLogger().info(mmForConsole("<gray>Download: </gray><#97F0E6>https://modrinth.com/plugin/serveressentials</#97F0E6>"));

                            } else {

                                Bukkit.getLogger().info(mmForConsole("<gradient:#FFCC33:#FFF2AA>ServerEssentials - Up To Date</gradient>"));
                                Bukkit.getLogger().info("§8--------------------------------");

                                Bukkit.getLogger().info(mmForConsole("<gray>Running latest version: </gray><green>" + currentVersion + "</green>"));
                            }

                            Bukkit.getLogger().info("§8================================");
                            Bukkit.getLogger().info("");

                        }
                    } catch (Exception e) {
                        Bukkit.getLogger().warning("§8================================");
                        Bukkit.getLogger().warning(mmForConsole("<red>Failed to parse version data from Modrinth.</red>"));
                        Bukkit.getLogger().warning("§8================================");
                    }
                })
                .exceptionally(e -> {
                    Bukkit.getLogger().warning("§8================================");
                    Bukkit.getLogger().warning(mmForConsole("<red>Could not fetch latest version from Modrinth.</red>"));
                    Bukkit.getLogger().warning("§8================================");
                    return null;
                });
    }

    /**
     * Notify OPs or players with permission
     */
    public static void notifyIfOutdated(Player player) {
        String currentVersion = ServerEssentials.getInstance().getDescription().getVersion();
        String latest = getLatestVersion();

        if (!latest.equals("unknown") && !currentVersion.equals(latest)) {
            if (player.hasPermission("serveressentials.version.notify") || player.isOp()) {

                player.sendMessage(MINI.deserialize("<gray> </gray>"));
                player.sendMessage(MINI.deserialize("<gray>8================================</gray>"));

                player.sendMessage(MINI.deserialize("<gradient:#FFCC33:#FFF2AA>ServerEssentials - Update Available</gradient>"));
                player.sendMessage(MINI.deserialize("<gray>--------------------------------</gray>"));

                player.sendMessage(MINI.deserialize("<gray>Current: </gray><red>" + currentVersion + "</red>"));
                player.sendMessage(MINI.deserialize("<gray>Latest: </gray><green>" + latest + "</green>"));
                player.sendMessage(MINI.deserialize("<gray>Download: </gray><aqua>https://modrinth.com/plugin/serveressentials</aqua>"));

                player.sendMessage(MINI.deserialize("<gray>================================</gray>"));
                player.sendMessage(MINI.deserialize("<gray> </gray>"));
            }
        }
    }

    public static String getCurrentVersion() {
        return ServerEssentials.getInstance().getDescription().getVersion();
    }

    public static String getLatestVersion() {
        return latestVersion;
    }
}
