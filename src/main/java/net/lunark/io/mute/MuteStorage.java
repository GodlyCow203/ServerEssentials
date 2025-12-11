package net.lunark.io.mute;

import net.lunark.io.database.DatabaseManager;
import org.bukkit.plugin.Plugin;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class MuteStorage {
    private final DatabaseManager dbManager;
    private final String poolKey = "mutes";
    private final Plugin plugin;

    public MuteStorage(Plugin plugin, DatabaseManager dbManager) {
        this.plugin = plugin;
        this.dbManager = dbManager;
        initTable();
    }

    private void initTable() {
        String sql = "CREATE TABLE IF NOT EXISTS mutes (" +
                "uuid TEXT PRIMARY KEY, " +
                "reason TEXT NOT NULL, " +
                "expires_at BIGINT NOT NULL)";
        dbManager.executeUpdate(poolKey, sql);
    }

    public CompletableFuture<Void> mutePlayer(UUID uuid, String reason, long expiresAt) {
        String sql = "INSERT OR REPLACE INTO mutes VALUES (?, ?, ?)";
        return dbManager.executeUpdate(poolKey, sql, uuid.toString(), reason, expiresAt);
    }

    public CompletableFuture<Void> unmutePlayer(UUID uuid) {
        String sql = "DELETE FROM mutes WHERE uuid = ?";
        return dbManager.executeUpdate(poolKey, sql, uuid.toString());
    }

    public CompletableFuture<Boolean> isMuted(UUID uuid) {
        String sql = "SELECT expires_at FROM mutes WHERE uuid = ?";
        return dbManager.executeQuery(poolKey, sql,
                        rs -> rs.next() ? rs.getLong("expires_at") : null,
                        uuid.toString())
                .thenApply(expiresAt -> {
                    if (expiresAt == null) return false;
                    if (expiresAt == -1) return true;
                    return System.currentTimeMillis() <= expiresAt;
                });
    }

    public CompletableFuture<Optional<MuteData>> getMuteData(UUID uuid) {
        String sql = "SELECT * FROM mutes WHERE uuid = ?";
        return dbManager.executeQuery(poolKey, sql, this::mapMuteData, uuid.toString());
    }

    public CompletableFuture<Set<UUID>> getAllMutedUUIDs() {
        String sql = "SELECT uuid FROM mutes";
        return dbManager.executeQuery(poolKey, sql, rs -> {
            Set<UUID> uuids = new HashSet<>();
            while (rs.next()) {
                uuids.add(UUID.fromString(rs.getString("uuid")));
            }
            return uuids;
        }).thenApply(opt -> opt.orElse(Collections.emptySet()));
    }

    private Optional<MuteData> mapMuteData(ResultSet rs) throws SQLException {
        if (!rs.next()) return Optional.empty();
        return Optional.of(new MuteData(
                UUID.fromString(rs.getString("uuid")),
                rs.getString("reason"),
                rs.getLong("expires_at")
        ));
    }

    public record MuteData(UUID uuid, String reason, long expiresAt) {}
}