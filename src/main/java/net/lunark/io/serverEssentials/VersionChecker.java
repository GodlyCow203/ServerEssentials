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

public class VersionChecker {

    private static final String MODRINTH_PROJECT_ID = "K7HZMVgx";
    private static String latestVersion = "unknown";

    private static final MiniMessage MINI = MiniMessage.miniMessage();


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

                                Bukkit.getLogger().info("§eServerEssentials §7- §6Update Available");
                                Bukkit.getLogger().info("§8--------------------------------");

                                Bukkit.getLogger().info("§7Current: §c" + currentVersion);
                                Bukkit.getLogger().info("§7Latest: §a" + latestVersion);
                                Bukkit.getLogger().info("§7Download: §bhttps://modrinth.com/plugin/serveressentials");

                            } else {

                                Bukkit.getLogger().info("§eServerEssentials §7- §aUp To Date");
                                Bukkit.getLogger().info("§8--------------------------------");

                                Bukkit.getLogger().info("§7Running latest version: §a" + currentVersion);
                            }

                            Bukkit.getLogger().info("§8================================");
                            Bukkit.getLogger().info("");

                        }
                    } catch (Exception e) {
                        Bukkit.getLogger().warning("§8================================");
                        Bukkit.getLogger().warning("§cFailed to parse version data from Modrinth.");
                        Bukkit.getLogger().warning("§8================================");
                    }
                })
                .exceptionally(e -> {
                    Bukkit.getLogger().warning("§8================================");
                    Bukkit.getLogger().warning("§cCould not fetch latest version from Modrinth.");
                    Bukkit.getLogger().warning("§8================================");
                    return null;
                });
    }


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
