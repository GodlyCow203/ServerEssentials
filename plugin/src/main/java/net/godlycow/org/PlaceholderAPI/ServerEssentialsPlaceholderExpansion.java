package net.godlycow.org.PlaceholderAPI;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import net.godlycow.org.EssC;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ServerEssentialsPlaceholderExpansion extends PlaceholderExpansion {

    private final EssC plugin;

    public ServerEssentialsPlaceholderExpansion(EssC plugin) {
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
        return "2.0.8.1";
    }

    @Override
    public boolean persist() {
        return true;
    }

    public String onPlaceholderRequest(OfflinePlayer offlinePlayer, @NotNull String identifier) {
        if (identifier == null) return "";

        String id = identifier.toLowerCase();
        if (id.startsWith("playtime_top_")) {
            return handleTopPlaytimePlaceholder(id);
        }

        if (offlinePlayer == null || !offlinePlayer.isOnline()) {
            return "";
        }

        Player player = offlinePlayer.getPlayer();
        if (player == null) {
            return "";
        }

        switch (id) {
            case "playtime":
                return formatTime(getPlaytimeInSeconds(player));

            case "isop":
                return player.isOp() ? "Yes" : "No";

            case "deaths":
                return String.valueOf(getStatisticSafe(player, Statistic.DEATHS));

            case "blocks_broken":
                return String.valueOf(calculateBlocksBroken(player));

            case "blocks_placed":
                return String.valueOf(calculateBlocksPlaced(player));

            case "kills":
                return String.valueOf(getStatisticSafe(player, Statistic.PLAYER_KILLS));

            default:
                return "";
        }
    }

    private long getPlaytimeInSeconds(Player player) {
        try {
            long ticks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
            return ticks / 20;
        } catch (Exception e) {
            return 0;
        }
    }

    private int getStatisticSafe(Player player, Statistic statistic) {
        try {
            return player.getStatistic(statistic);
        } catch (Exception e) {
            return 0;
        }
    }

    private int calculateBlocksBroken(Player player) {
        int broken = 0;
        for (Material material : Material.values()) {
            try {
                broken += player.getStatistic(Statistic.MINE_BLOCK, material);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return broken;
    }

    private int calculateBlocksPlaced(Player player) {
        int placed = 0;
        for (Material material : Material.values()) {
            try {
                placed += player.getStatistic(Statistic.USE_ITEM, material);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return placed;
    }

    private String handleTopPlaytimePlaceholder(String identifier) {
        try {
            int rank = Integer.parseInt(identifier.substring("playtime_top_".length()));
            List<Map.Entry<UUID, Long>> top = plugin.getPlaytimeManager().getTopPlaytimes(rank);

            if (rank > 0 && rank <= top.size()) {
                Map.Entry<UUID, Long> entry = top.get(rank - 1);
                UUID targetUUID = entry.getKey();
                long seconds = entry.getValue();

                OfflinePlayer topPlayer = Bukkit.getOfflinePlayer(targetUUID);
                String name = topPlayer.getName() != null ? topPlayer.getName() : "Unknown";
                return name + ": " + formatTime(seconds);
            } else {
                return "N/A";
            }
        } catch (NumberFormatException e) {
            return "Invalid number";
        } catch (Exception e) {
            return "Error";
        }
    }

    private String formatTime(long totalSeconds) {
        if (totalSeconds < 0) return "0s";

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
}