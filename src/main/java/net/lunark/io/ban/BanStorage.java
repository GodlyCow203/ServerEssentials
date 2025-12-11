package net.lunark.io.ban;

import net.lunark.io.database.DatabaseManager;
import org.bukkit.plugin.Plugin;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class BanStorage {
    private final DatabaseManager dbManager;
    private final String poolKey = "bans";
    private final Plugin plugin;

    public BanStorage(Plugin plugin, DatabaseManager dbManager) {
        this.plugin = plugin;
        this.dbManager = dbManager;
        initTable();
    }

    private void initTable() {
        String sql = "CREATE TABLE IF NOT EXISTS bans (" +
                "uuid TEXT PRIMARY KEY, " +
                "name TEXT NOT NULL, " +
                "reason TEXT NOT NULL, " +
                "banned_by TEXT NOT NULL, " +
                "banned_until BIGINT NOT NULL, " +
                "server TEXT NOT NULL, " +
                "discord TEXT NOT NULL)";
        dbManager.executeUpdate(poolKey, sql);
    }

    public CompletableFuture<Void> banPlayer(UUID uuid, String name, String reason,
                                             String bannedBy, long until, String server, String discord) {
        String sql = "INSERT OR REPLACE INTO bans VALUES (?, ?, ?, ?, ?, ?, ?)";
        return dbManager.executeUpdate(poolKey, sql,
                uuid.toString(), name, reason, bannedBy, until, server, discord);
    }

    public CompletableFuture<Boolean> isBanned(UUID uuid) {
        String sql = "SELECT banned_until FROM bans WHERE uuid = ?";
        return dbManager.executeQuery(poolKey, sql,
                        rs -> rs.next() ? rs.getLong("banned_until") : null,
                        uuid.toString())
                .thenApply(until -> {
                    if (until == null) return false;
                    if (until == -1) return true;
                    return System.currentTimeMillis() <= until;
                });
    }

    public CompletableFuture<Void> unbanPlayer(UUID uuid) {
        String sql = "DELETE FROM bans WHERE uuid = ?";
        return dbManager.executeUpdate(poolKey, sql, uuid.toString());
    }

    public CompletableFuture<Optional<BanData>> getBanData(UUID uuid) {
        String sql = "SELECT * FROM bans WHERE uuid = ?";
        return dbManager.executeQuery(poolKey, sql, this::mapBanData, uuid.toString());
    }

    public CompletableFuture<Optional<UUID>> getUUIDFromName(String name) {
        String sql = "SELECT uuid FROM bans WHERE LOWER(name) = LOWER(?)";
        return dbManager.executeQuery(poolKey, sql,
                rs -> rs.next() ? UUID.fromString(rs.getString("uuid")) : null,
                name);
    }

    public CompletableFuture<Set<String>> getAllBannedUUIDs() {
        String sql = "SELECT uuid FROM bans";
        return dbManager.executeQuery(poolKey, sql, rs -> {
            Set<String> uuids = new HashSet<>();
            while (rs.next()) {
                uuids.add(rs.getString("uuid"));
            }
            return uuids;
        }).thenApply(opt -> opt.orElse(Collections.emptySet()));
    }

    public CompletableFuture<Optional<String>> getNameFromUUID(String uuid) {
        String sql = "SELECT name FROM bans WHERE uuid = ?";
        return dbManager.executeQuery(poolKey, sql,
                rs -> rs.next() ? rs.getString("name") : null,
                uuid);
    }

    public CompletableFuture<Optional<String>> getReason(UUID uuid) {
        String sql = "SELECT reason FROM bans WHERE uuid = ?";
        return dbManager.executeQuery(poolKey, sql,
                rs -> rs.next() ? rs.getString("reason") : null,
                uuid.toString());
    }

    public CompletableFuture<Optional<Long>> getUntil(UUID uuid) {
        String sql = "SELECT banned_until FROM bans WHERE uuid = ?";
        return dbManager.executeQuery(poolKey, sql,
                rs -> rs.next() ? rs.getLong("banned_until") : null,
                uuid.toString());
    }

    private Optional<BanData> mapBanData(ResultSet rs) throws SQLException {
        if (!rs.next()) return Optional.empty();
        return Optional.of(new BanData(
                UUID.fromString(rs.getString("uuid")),
                rs.getString("name"),
                rs.getString("reason"),
                rs.getString("banned_by"),
                rs.getLong("banned_until"),
                rs.getString("server"),
                rs.getString("discord")
        ));
    }

    public record BanData(UUID uuid, String name, String reason, String bannedBy,
                          long until, String server, String discord) {}
}