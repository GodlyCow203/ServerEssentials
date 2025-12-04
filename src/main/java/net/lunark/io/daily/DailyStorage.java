package net.lunark.io.daily;

import com.zaxxer.hikari.HikariDataSource;
import net.lunark.io.database.DatabaseManager;
import org.bukkit.plugin.Plugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class DailyStorage {
    private final DatabaseManager dbManager;
    private final String poolKey = "daily";
    private final Plugin plugin;

    public DailyStorage(Plugin plugin, DatabaseManager dbManager) {
        this.plugin = plugin;
        this.dbManager = dbManager;
        initTable();
    }

    private void initTable() {
        String sql = "CREATE TABLE IF NOT EXISTS daily_claims (" +
                "player_uuid TEXT NOT NULL, " +
                "day INTEGER NOT NULL, " +
                "claimed_at BIGINT NOT NULL, " +
                "PRIMARY KEY (player_uuid, day))";
        dbManager.executeUpdate(poolKey, sql).join();
    }

    public CompletableFuture<Set<Integer>> getClaimedDays(UUID playerId) {
        String sql = "SELECT day FROM daily_claims WHERE player_uuid = ?";
        return dbManager.executeQuery(poolKey, sql, rs -> {
            Set<Integer> days = new HashSet<>();
            while (rs.next()) {
                days.add(rs.getInt("day"));
            }
            return days;
        }, playerId.toString()).thenApply(opt -> opt.orElse(Set.of()));
    }

    public CompletableFuture<Optional<LocalDateTime>> getLastClaimTime(UUID playerId) {
        String sql = "SELECT MAX(claimed_at) as last_claim FROM daily_claims WHERE player_uuid = ?";
        return dbManager.executeQuery(poolKey, sql, rs -> {
            if (rs.next() && rs.getLong("last_claim") > 0) {
                return LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(rs.getLong("last_claim")),
                        ZoneId.systemDefault()
                );
            }
            return null;
        }, playerId.toString());
    }

    public CompletableFuture<Boolean> hasClaimedToday(UUID playerId, int cooldownHours) {
        return getLastClaimTime(playerId).thenApply(opt -> {
            if (opt.isEmpty()) return false;
            LocalDateTime lastClaim = opt.get();
            LocalDateTime now = LocalDateTime.now();
            return !lastClaim.plusHours(cooldownHours).isBefore(now);
        });
    }

    public CompletableFuture<Void> claimReward(UUID playerId, int day) {
        String sql = "INSERT OR REPLACE INTO daily_claims VALUES (?, ?, ?)";
        return dbManager.executeUpdate(poolKey, sql,
                playerId.toString(), day, System.currentTimeMillis());
    }

    public CompletableFuture<DurationInfo> getTimeUntilNextClaim(UUID playerId, int cooldownHours) {
        return getLastClaimTime(playerId).thenApply(opt -> {
            LocalDateTime now = LocalDateTime.now();
            if (opt.isEmpty()) {
                return new DurationInfo(0, 0, 0, false);
            }
            LocalDateTime nextAllowed = opt.get().plusHours(cooldownHours);
            java.time.Duration remaining = java.time.Duration.between(now, nextAllowed);

            boolean isOnCooldown = !remaining.isNegative();
            return new DurationInfo(
                    isOnCooldown ? remaining.toHoursPart() : 0,
                    isOnCooldown ? remaining.toMinutesPart() : 0,
                    isOnCooldown ? remaining.toSecondsPart() : 0,
                    isOnCooldown
            );
        });
    }

    public record DurationInfo(long hours, long minutes, long seconds, boolean onCooldown) {
        public String format() {
            return String.format("%02dh %02dm %02ds", hours, minutes, seconds);
        }
    }
}