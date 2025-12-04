package net.lunark.io.PlaceholderAPI;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import net.lunark.io.ServerEssentials;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TopPlaytimePlaceholder extends PlaceholderExpansion {

    private final ServerEssentials plugin;

    public TopPlaytimePlaceholder(ServerEssentials plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "serveressentials";
    }

    @Override
    public @NotNull String getAuthor() {
        return ":_GodlyCow";
    }

    @Override
    public @NotNull String getVersion() {
        return "2.0";
    }

    @Override
    public boolean persist() {
        return true;
    }


    public String onPlaceholderRequest(OfflinePlayer player, @NotNull String identifier) {
        if (identifier.toLowerCase().startsWith("playtime_top_")) {
            try {
                int rank = Integer.parseInt(identifier.substring("playtime_top_".length()));
                List<Map.Entry<UUID, Long>> top = plugin.getPlaytimeManager().getTopPlaytimes(rank);

                if (rank > 0 && rank <= top.size()) {
                    UUID targetUUID = top.get(rank - 1).getKey();
                    long seconds = top.get(rank - 1).getValue();

                    OfflinePlayer topPlayer = Bukkit.getOfflinePlayer(targetUUID);
                    String name = topPlayer.getName() != null ? topPlayer.getName() : "Unknown";
                    return name + ": " + formatTime(seconds);
                } else {
                    return "N/A";
                }
            } catch (NumberFormatException e) {
                return "Invalid number";
            }
        }

        return null;
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
}
