package serveressentials.serveressentials;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ServerEssentialsPlaceholder extends PlaceholderExpansion {

    private final ServerEssentials plugin;

    public ServerEssentialsPlaceholder(ServerEssentials plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "se";  // changed from "serveressentials" to "se"
    }

    @Override
    public @NotNull String getAuthor() {
        return ":_GodlyCow";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.8";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (identifier == null) return "";

        // Top Playtime: %se_playtime_top_<rank>%
        if (identifier.toLowerCase().startsWith("playtime_top_")) {
            try {
                int rank = Integer.parseInt(identifier.substring("playtime_top_".length()));
                List<Map.Entry<UUID, Long>> top = plugin.getPlaytimeManager().getTopPlaytimes(rank);

                if (rank > 0 && rank <= top.size()) {
                    UUID uuid = top.get(rank - 1).getKey();
                    long seconds = top.get(rank - 1).getValue();
                    OfflinePlayer target = Bukkit.getOfflinePlayer(uuid);
                    String name = target.getName() != null ? target.getName() : "Unknown";
                    return "#" + rank + " " + name + ": " + formatTime(seconds);
                } else {
                    return "N/A";
                }
            } catch (NumberFormatException e) {
                return "Invalid Rank";
            }
        }

        // Top Money: %se_money_top_<rank>%
        if (identifier.toLowerCase().startsWith("money_top_")) {
            try {
                int rank = Integer.parseInt(identifier.substring("money_top_".length()));
                List<Map.Entry<UUID, Double>> topBalances = EconomyManager.getTopBalances(rank);

                if (rank > 0 && rank <= topBalances.size()) {
                    UUID uuid = topBalances.get(rank - 1).getKey();
                    double balance = topBalances.get(rank - 1).getValue();
                    OfflinePlayer target = Bukkit.getOfflinePlayer(uuid);
                    String name = target.getName() != null ? target.getName() : "Unknown";
                    return "#" + rank + " " + name + ": $" + String.format("%.2f", balance);
                } else {
                    return "N/A";
                }
            } catch (NumberFormatException e) {
                return "Invalid Rank";
            }
        }

        // Regular player-based placeholders
        if (player == null) return "";

        switch (identifier.toLowerCase()) {
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

            case "rank":
                return getPlayerRank(player);

            case "money":
                double balance = EconomyManager.getBalance(player);
                return String.format("%.2f", balance);

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

    private String getPlayerRank(Player player) {
        RankManager rankManager = plugin.getRankManager();
        Rank rank = rankManager.getRank(player);
        String prefix = (rank != null) ? rank.getPrefix() : "Member";
        return ChatColor.stripColor(prefix); // strips legacy § codes
    }
}
