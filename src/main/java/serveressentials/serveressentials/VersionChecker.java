package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class VersionChecker {

    private static final String PREFIX = ChatColor.BLUE.toString() + ChatColor.BOLD + "[" +
            ChatColor.AQUA + "SE" +
            ChatColor.BLUE + ChatColor.BOLD + "] " + ChatColor.RESET;

    private static final String MODRINTH_PROJECT_ID = "K7HZMVgx";
    private static String latestVersion = "unknown";

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
                                Bukkit.getLogger().warning(PREFIX + ChatColor.YELLOW + "A new version is available: v" + latestVersion);
                                Bukkit.getLogger().warning(PREFIX + ChatColor.GRAY + "You are currently running v" + currentVersion);
                            }
                        }
                    } catch (Exception e) {
                        Bukkit.getLogger().warning(PREFIX + ChatColor.RED + "Failed to parse version data.");
                        e.printStackTrace();
                    }
                })
                .exceptionally(e -> {
                    Bukkit.getLogger().warning(PREFIX + ChatColor.RED + "Could not fetch latest version from Modrinth.");
                    e.printStackTrace();
                    return null;
                });
    }

    public static void notifyIfOutdated(Player player) {
        String currentVersion = ServerEssentials.getInstance().getDescription().getVersion();
        String latest = getLatestVersion();

        if (!latest.equals("unknown") && !currentVersion.equals(latest)) {
            if (player.hasPermission("serveressentials.version.notify") || player.isOp()) {
                player.sendMessage(PREFIX + ChatColor.YELLOW + "A new version (" + latest + ") is available!");
                player.sendMessage(PREFIX + ChatColor.GRAY + "Download: " + ChatColor.AQUA + "https://modrinth.com/plugin/serveressentials");
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
