package net.godlycow.org.reports.storage;

import net.godlycow.org.database.DatabaseManager;
import net.godlycow.org.reports.model.Report;
import org.bukkit.plugin.Plugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ReportStorage {
    private final DatabaseManager dbManager;
    private final String poolKey = "reports";
    private final Plugin plugin;

    public ReportStorage(Plugin plugin, DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.plugin = plugin;
        initTable();
    }

    private void initTable() {
        String tableSql = "CREATE TABLE IF NOT EXISTS reports (" +
                "id TEXT PRIMARY KEY, " +
                "reporter_uuid TEXT NOT NULL, " +
                "target_uuid TEXT NOT NULL, " +
                "reason TEXT NOT NULL, " +
                "timestamp BIGINT NOT NULL, " +
                "pending BOOLEAN DEFAULT TRUE)";

        dbManager.executeUpdate(poolKey, tableSql)
                .thenRun(() -> {
                    String indexSql = "CREATE INDEX IF NOT EXISTS idx_pending ON reports(pending)";
                    dbManager.executeUpdate(poolKey, indexSql).exceptionally(ex -> {
                        plugin.getLogger().warning("Failed to create reports index: " + ex.getMessage());
                        return null;
                    });

                    String cooldownTableSql = "CREATE TABLE IF NOT EXISTS report_cooldowns (" +
                            "player_uuid TEXT PRIMARY KEY, " +
                            "timestamp BIGINT)";
                    dbManager.executeUpdate(poolKey, cooldownTableSql).exceptionally(ex -> {
                        plugin.getLogger().warning("Failed to create report cooldown table: " + ex.getMessage());
                        return null;
                    });
                })
                .exceptionally(ex -> {
                    plugin.getLogger().severe("Failed to create reports table: " + ex.getMessage());
                    return null;
                });
    }

    public CompletableFuture<Void> addReport(Report report) {
        String sql = "INSERT INTO reports (id, reporter_uuid, target_uuid, reason, timestamp, pending) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        return dbManager.executeUpdate(poolKey, sql,
                report.id(),
                report.reporterId().toString(),
                report.targetId().toString(),
                report.reason(),
                report.timestamp(),
                report.pending()
        );
    }

    public CompletableFuture<Optional<Report>> getReport(String id) {
        String sql = "SELECT * FROM reports WHERE id = ?";
        return dbManager.executeQuery(poolKey, sql, this::mapReport, id);
    }

    public CompletableFuture<List<Report>> getAllReports() {
        String sql = "SELECT * FROM reports ORDER BY timestamp DESC";
        return dbManager.executeQuery(poolKey, sql, rs -> {
            List<Report> reports = new ArrayList<>();
            while (rs.next()) {
                reports.add(mapReport(rs));
            }
            return reports;
        }).thenApply(opt -> opt.orElse(new ArrayList<>()));
    }

    public CompletableFuture<List<Report>> getPendingReports() {
        String sql = "SELECT * FROM reports WHERE pending = TRUE ORDER BY timestamp DESC";
        return dbManager.executeQuery(poolKey, sql, rs -> {
            List<Report> reports = new ArrayList<>();
            while (rs.next()) {
                reports.add(mapReport(rs));
            }
            return reports;
        }).thenApply(opt -> opt.orElse(new ArrayList<>()));
    }

    public CompletableFuture<Void> clearReport(String id) {
        String sql = "DELETE FROM reports WHERE id = ?";
        return dbManager.executeUpdate(poolKey, sql, id);
    }

    public CompletableFuture<Void> markAsCleared(String id) {
        String sql = "UPDATE reports SET pending = FALSE WHERE id = ?";
        return dbManager.executeUpdate(poolKey, sql, id);
    }

    private Report mapReport(ResultSet rs) throws SQLException {
        return new Report(
                rs.getString("id"),
                UUID.fromString(rs.getString("reporter_uuid")),
                UUID.fromString(rs.getString("target_uuid")),
                rs.getString("reason"),
                rs.getLong("timestamp"),
                rs.getBoolean("pending")
        );
    }

    public CompletableFuture<Void> saveCooldown(UUID playerId, long timestamp) {
        String sql = "INSERT OR REPLACE INTO report_cooldowns (player_uuid, timestamp) VALUES (?, ?)";
        return dbManager.executeUpdate(poolKey, sql, playerId.toString(), timestamp);
    }

    public CompletableFuture<Long> getCooldown(UUID playerId) {
        String sql = "SELECT timestamp FROM report_cooldowns WHERE player_uuid = ?";
        return dbManager.executeQuery(poolKey, sql, rs -> rs.next() ? rs.getLong("timestamp") : 0L,
                playerId.toString()).thenApply(opt -> opt.orElse(0L));
    }
}