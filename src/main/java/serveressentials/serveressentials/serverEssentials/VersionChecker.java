package serveressentials.serveressentials.serverEssentials;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.json.JSONArray;
import org.json.JSONObject;
import serveressentials.serveressentials.ServerEssentials;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class VersionChecker {

    private static final String MODRINTH_PROJECT_ID = "K7HZMVgx";
    private static String latestVersion = "unknown";

    /**
     * Checks the latest plugin version on Modrinth asynchronously
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

                            if (!currentVersion.equalsIgnoreCase(latestVersion)) {
                                Bukkit.getLogger().info("================================");
                                Bukkit.getLogger().info("  ServerEssentials - Update");
                                Bukkit.getLogger().info("--------------------------------");
                                Bukkit.getLogger().info(" Current: " + currentVersion);
                                Bukkit.getLogger().info(" Latest : " + latestVersion);
                                Bukkit.getLogger().info(" Download: https://modrinth.com/plugin/serveressentials");
                                Bukkit.getLogger().info("================================");
                            } else {
                                Bukkit.getLogger().info("================================");
                                Bukkit.getLogger().info("  ServerEssentials - Version");
                                Bukkit.getLogger().info("--------------------------------");
                                Bukkit.getLogger().info(" You are running the latest version: " + currentVersion);
                                Bukkit.getLogger().info("================================");
                            }
                        }
                    } catch (Exception e) {
                        Bukkit.getLogger().warning("================================");
                        Bukkit.getLogger().warning(" Failed to parse version data from Modrinth.");
                        Bukkit.getLogger().warning("================================");
                    }
                })
                .exceptionally(e -> {
                    Bukkit.getLogger().warning("================================");
                    Bukkit.getLogger().warning(" Could not fetch latest version from Modrinth.");
                    Bukkit.getLogger().warning("================================");
                    return null;
                });
    }

    /**
     * Notify a player if their plugin version is outdated
     */
    public static void notifyIfOutdated(Player player) {
        String currentVersion = ServerEssentials.getInstance().getDescription().getVersion();
        String latest = getLatestVersion();

        if (!latest.equals("unknown") && !currentVersion.equals(latest)) {
            if (player.hasPermission("serveressentials.version.notify") || player.isOp()) {
                player.sendMessage("§8================================");
                player.sendMessage("§f §lServerEssentials - Update"); // bold
                player.sendMessage("§8--------------------------------");
                player.sendMessage("§7 Current: §o§4" + currentVersion); // italic + red
                player.sendMessage("§7 Latest : §o§a" + latest);        // italic + green
                player.sendMessage("§7 Download: §9https://modrinth.com/plugin/serveressentials");
                player.sendMessage("§8================================");
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
