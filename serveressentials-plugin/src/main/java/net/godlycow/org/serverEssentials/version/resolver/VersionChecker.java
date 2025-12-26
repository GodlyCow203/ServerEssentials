package net.godlycow.org.serverEssentials.version.resolver;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.json.JSONArray;
import org.json.JSONObject;
import net.godlycow.org.ServerEssentials;
import net.godlycow.org.util.logger.AnsiColorUtil;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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

                            String graySep = AnsiColorUtil.colorize(AnsiColorUtil.GRAY, "================================");
                            String grayDash = AnsiColorUtil.colorize(AnsiColorUtil.GRAY, "--------------------------------");

                            Bukkit.getLogger().info("");
                            Bukkit.getLogger().info(graySep);

                            if (!currentVersion.equalsIgnoreCase(latestVersion)) {
                                Bukkit.getLogger().info(
                                        AnsiColorUtil.warning("ServerEssentials") +
                                                AnsiColorUtil.colorize(AnsiColorUtil.GRAY, " - ") +
                                                AnsiColorUtil.warning("Update Available")
                                );
                                Bukkit.getLogger().info(grayDash);

                                Bukkit.getLogger().info(AnsiColorUtil.secondary("Current: ") + AnsiColorUtil.danger(currentVersion));
                                Bukkit.getLogger().info(AnsiColorUtil.secondary("Latest: ") + AnsiColorUtil.success(latestVersion));
                                Bukkit.getLogger().info(AnsiColorUtil.secondary("Download: ") + AnsiColorUtil.info("https://modrinth.com/plugin/serveressentials"));

                            } else {
                                Bukkit.getLogger().info(
                                        AnsiColorUtil.warning("ServerEssentials") +
                                                AnsiColorUtil.colorize(AnsiColorUtil.GRAY, " - ") +
                                                AnsiColorUtil.success("Up To Date")
                                );
                                Bukkit.getLogger().info(grayDash);

                                Bukkit.getLogger().info(AnsiColorUtil.secondary("Running latest version: ") + AnsiColorUtil.success(currentVersion));
                            }

                            Bukkit.getLogger().info(graySep);
                            Bukkit.getLogger().info("");

                        }
                    } catch (Exception e) {
                        String graySep = AnsiColorUtil.colorize(AnsiColorUtil.GRAY, "================================");
                        Bukkit.getLogger().warning(graySep);
                        Bukkit.getLogger().warning(AnsiColorUtil.danger("Failed to parse version data from Modrinth."));
                        Bukkit.getLogger().warning(graySep);
                    }
                })
                .exceptionally(e -> {
                    String graySep = AnsiColorUtil.colorize(AnsiColorUtil.GRAY, "================================");
                    Bukkit.getLogger().warning(graySep);
                    Bukkit.getLogger().warning(AnsiColorUtil.danger("Could not fetch latest version from Modrinth."));
                    Bukkit.getLogger().warning(graySep);
                    return null;
                });
    }

    public static void notifyIfOutdated(Player player) {
        String currentVersion = ServerEssentials.getInstance().getDescription().getVersion();
        String latest = getLatestVersion();

        if (!latest.equals("unknown") && !currentVersion.equals(latest)) {
            if (player.hasPermission("serveressentials.version.notify") || player.isOp()) {

                player.sendMessage(MINI.deserialize("<gray> </gray>"));
                player.sendMessage(MINI.deserialize("<gray>================================</gray>"));

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