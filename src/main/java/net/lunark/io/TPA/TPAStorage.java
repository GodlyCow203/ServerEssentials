package net.lunark.io.TPA;

import net.lunark.io.database.DatabaseManager;
import org.bukkit.plugin.Plugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class TPAStorage {
    private final DatabaseManager dbManager;
    private final String poolKey = "tpa";
    private final Plugin plugin;

    public TPAStorage(Plugin plugin, DatabaseManager dbManager) {
        this.plugin = plugin;
        this.dbManager = dbManager;
        initTables();
    }

    private void initTables() {
        String sql = "CREATE TABLE IF NOT EXISTS tpa_data (" +
                "type TEXT, " + // 'cooldown', 'toggle'
                "player_uuid TEXT, " +
                "target_uuid TEXT, " + // for requests
                "data TEXT, " +
                "timestamp BIGINT, " +
                "PRIMARY KEY (type, player_uuid, target_uuid))";

        dbManager.executeUpdate(poolKey, sql).exceptionally(ex -> {
            plugin.getLogger().severe("Failed to create TPA table: " + ex.getMessage());
            return null;
        });
    }

    // Cooldown operations
    public CompletableFuture<Void> saveCooldown(UUID playerId, long timestamp) {
        String sql = "INSERT OR REPLACE INTO tpa_data (type, player_uuid, data, timestamp) VALUES (?, ?, ?, ?)";
        return dbManager.executeUpdate(poolKey, sql, "cooldown", playerId.toString(), null, timestamp);
    }

    public CompletableFuture<Long> getCooldown(UUID playerId) {
        String sql = "SELECT timestamp FROM tpa_data WHERE type = ? AND player_uuid = ?";
        return dbManager.executeQuery(poolKey, sql, rs -> rs.next() ? rs.getLong("timestamp") : 0L,
                        "cooldown", playerId.toString())
                .thenApply(opt -> opt.orElse(0L));
    }

    // Toggle operations
    public CompletableFuture<Void> setToggle(UUID playerId, boolean enabled) {
        String sql = "INSERT OR REPLACE INTO tpa_data (type, player_uuid, data) VALUES (?, ?, ?)";
        return dbManager.executeUpdate(poolKey, sql, "toggle", playerId.toString(), String.valueOf(enabled));
    }

    public CompletableFuture<Boolean> getToggle(UUID playerId) {
        String sql = "SELECT data FROM tpa_data WHERE type = ? AND player_uuid = ?";
        return dbManager.executeQuery(poolKey, sql, rs -> rs.next() ? Boolean.parseBoolean(rs.getString("data")) : false,
                        "toggle", playerId.toString())
                .thenApply(opt -> opt.orElse(false));
    }

    // Active request storage
    public CompletableFuture<Void> saveRequest(TPARequest request) {
        String sql = "INSERT OR REPLACE INTO tpa_data (type, player_uuid, target_uuid, data, timestamp) VALUES (?, ?, ?, ?, ?)";
        String data = request.here + ":" + request.cost;
        return dbManager.executeUpdate(poolKey, sql, "request",
                request.senderId.toString(),
                request.targetId.toString(),
                data,
                System.currentTimeMillis());
    }

    public CompletableFuture<List<TPARequest>> getActiveRequestsForSender(UUID senderId) {
        String sql = "SELECT * FROM tpa_data WHERE type = ? AND player_uuid = ?";
        return dbManager.executeQuery(poolKey, sql, rs -> {
            List<TPARequest> requests = new ArrayList<>();
            while (rs.next()) {
                String[] data = rs.getString("data").split(":");
                requests.add(new TPARequest(
                        UUID.fromString(rs.getString("player_uuid")),
                        UUID.fromString(rs.getString("target_uuid")),
                        Boolean.parseBoolean(data[0]),
                        Double.parseDouble(data[1]),
                        rs.getLong("timestamp")
                ));
            }
            return requests;
        }, "request", senderId.toString()).thenApply(opt -> opt.orElse(Collections.emptyList()));
    }

    public CompletableFuture<Void> removeRequest(UUID senderId, UUID targetId) {
        String sql = "DELETE FROM tpa_data WHERE type = ? AND player_uuid = ? AND target_uuid = ?";
        return dbManager.executeUpdate(poolKey, sql, "request", senderId.toString(), targetId.toString());
    }

    public CompletableFuture<List<TPARequest>> getActiveRequests(UUID targetId) {
        String sql = "SELECT * FROM tpa_data WHERE type = ? AND target_uuid = ?";
        return dbManager.executeQuery(poolKey, sql, rs -> {
            List<TPARequest> requests = new ArrayList<>();
            while (rs.next()) {
                String[] data = rs.getString("data").split(":");
                requests.add(new TPARequest(
                        UUID.fromString(rs.getString("player_uuid")),
                        UUID.fromString(rs.getString("target_uuid")),
                        Boolean.parseBoolean(data[0]),
                        Double.parseDouble(data[1]),
                        rs.getLong("timestamp")
                ));
            }
            return requests;
        }, "request", targetId.toString()).thenApply(opt -> opt.orElse(Collections.emptyList()));
    }
}