package net.godlycow.org.mute.storage;

import net.godlycow.org.database.DatabaseManager;
import org.bukkit.plugin.Plugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;

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
        String sql = """
                CREATE TABLE IF NOT EXISTS mutes (
                    uuid TEXT PRIMARY KEY,
                    reason TEXT NOT NULL,
                    expires_at BIGINT NOT NULL
                )
                """;

        dbManager.executeUpdate(poolKey, sql);
    }

    public CompletableFuture<Void> mutePlayer(UUID uuid, String reason, long expiresAt) {
        String sql = "INSERT OR REPLACE INTO mutes VALUES (?, ?, ?)";
        return dbManager.executeUpdate(poolKey, sql,
                uuid.toString(), reason, expiresAt);
    }

    public CompletableFuture<Void> unmutePlayer(UUID uuid) {
        String sql = "DELETE FROM mutes WHERE uuid = ?";
        return dbManager.executeUpdate(poolKey, sql, uuid.toString());
    }

    public CompletableFuture<Boolean> isMuted(UUID uuid) {
        String sql = "SELECT expires_at FROM mutes WHERE uuid = ?";

        return dbManager.executeQuery(poolKey, sql,
                rs -> rs.next() ? rs.getLong("expires_at") : null,
                uuid.toString()
        ).thenApply(optExpires ->
                optExpires.map(expires -> {
                    if (expires == -1) return true;
                    return System.currentTimeMillis() <= expires;
                }).orElse(false)
        );
    }
    public CompletableFuture<Optional<MuteData>> getMuteData(UUID uuid) {
        String sql = "SELECT * FROM mutes WHERE uuid = ?";
        return dbManager.executeQuery(poolKey, sql, this::mapMuteData, uuid.toString());
    }

    private MuteData mapMuteData(ResultSet rs) throws SQLException {
        if (!rs.next()) return null;
        return new MuteData(
                UUID.fromString(rs.getString("uuid")),
                rs.getString("reason"),
                rs.getLong("expires_at")
        );
    }

    public CompletableFuture<Set<UUID>> getAllMutedUUIDs() {
        String sql = "SELECT uuid FROM mutes";

        return dbManager.executeQuery(poolKey, sql, rs -> {
            Set<UUID> uuids = new HashSet<>();
            while (rs.next()) {
                uuids.add(UUID.fromString(rs.getString("uuid")));
            }
            return uuids;
        }).thenApply(opt -> opt.orElseGet(HashSet::new));
    }

    public CompletableFuture<Optional<String>> getMuteReason(UUID uuid) {
        return getMuteData(uuid).thenApply(opt -> opt.map(MuteData::reason));
    }

    public boolean isMutedSync(UUID uuid) {
        try {
            return isMuted(uuid).get(2, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            plugin.getLogger().severe("Failed to check mute status for " + uuid + ": " + e.getMessage());
            return false;
        }
    }
    public Optional<MuteData> getMuteDataSync(UUID uuid) {
        try {
            return getMuteData(uuid).get(2, TimeUnit.SECONDS);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to retrieve mute data for " + uuid + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<String> getMuteReasonSync(UUID uuid) {
        try {
            return getMuteDataSync(uuid).map(MuteData::reason);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to retrieve mute reason for " + uuid + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    public record MuteData(UUID uuid, String reason, long expiresAt) {}
}