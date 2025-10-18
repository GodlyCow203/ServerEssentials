package serveressentials.serveressentials.PlaceholderAPI;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.Statistic;
import org.jetbrains.annotations.NotNull;

import serveressentials.serveressentials.ServerEssentials;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServerEssentialsPlaceholder extends PlaceholderExpansion {

    private final ServerEssentials plugin;

    public ServerEssentialsPlaceholder(ServerEssentials plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "se";
    }

    @Override
    public @NotNull String getAuthor() {
        return ":_GodlyCow";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.11";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (identifier == null) return "";

        String id = identifier.toLowerCase();

        // Handle top placeholders first


        // Regular player-based placeholders
        if (player == null) return "";

        switch (id) {
            case "playtime":
                long ticksPlayed = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
                long secondsPlayed = ticksPlayed / 20;
                return formatTime(secondsPlayed);

            case "isop":
                return player.isOp() ? "Yes" : "No";

            case "death":
                return String.valueOf(player.getStatistic(Statistic.DEATHS));

            case "blocks_broken":
                int broken = 0;
                for (Material material : Material.values()) {
                    try {
                        broken += player.getStatistic(Statistic.MINE_BLOCK, material);
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                return String.valueOf(broken);

            case "blocks_placed":
                int placed = 0;
                for (Material material : Material.values()) {
                    try {
                        placed += player.getStatistic(Statistic.USE_ITEM, material);
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                return String.valueOf(placed);

            case "kills":
                return String.valueOf(player.getStatistic(Statistic.PLAYER_KILLS));





            default:
                return "Unknown Placeholder: " + identifier;
        }
    }

    private String formatTime(long totalSeconds) {
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        sb.append(seconds).append("s");

        return sb.toString().trim();
    }



    // Quick method to get online players UUIDs (replace with your own storage for all players if available)
    private List<UUID> getAllPlayersUUIDs() {
        List<UUID> uuids = new ArrayList<>();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            uuids.add(onlinePlayer.getUniqueId());
        }
        return uuids;
    }



}
