package net.godlycow.org.ban.storage;

import net.godlycow.org.database.DatabaseManager;
import org.bukkit.plugin.Plugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
        String sql = """
                CREATE TABLE IF NOT EXISTS bans (
                    uuid TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    reason TEXT NOT NULL,
                    banned_by TEXT NOT NULL,
                    banned_until BIGINT NOT NULL,
                    server TEXT NOT NULL,
                    discord TEXT NOT NULL
                )
                """;

        dbManager.executeUpdate(poolKey, sql);
    }

    public CompletableFuture<Void> banPlayer(
            UUID uuid, String name, String reason,
            String bannedBy, long until, String server, String discord
    ) {
        String sql = "INSERT OR REPLACE INTO bans VALUES (?, ?, ?, ?, ?, ?, ?)";
        return dbManager.executeUpdate(poolKey, sql,
                uuid.toString(), name, reason, bannedBy, until, server, discord);
    }

    public CompletableFuture<Boolean> isBanned(UUID uuid) {
        String sql = "SELECT banned_until FROM bans WHERE uuid = ?";

        return dbManager.executeQuery(poolKey, sql,
                rs -> rs.next() ? rs.getLong("banned_until") : null,
                uuid.toString()
        ).thenApply(optUntil ->
                optUntil.map(until -> {
                    if (until == -1) return true;
                    return System.currentTimeMillis() <= until;
                }).orElse(false)
        );
    }

    public CompletableFuture<Void> unbanPlayer(UUID uuid) {
        String sql = "DELETE FROM bans WHERE uuid = ?";
        return dbManager.executeUpdate(poolKey, sql, uuid.toString());
    }

    public CompletableFuture<Optional<BanData>> getBanData(UUID uuid) {
        String sql = "SELECT * FROM bans WHERE uuid = ?";
        return dbManager.executeQuery(poolKey, sql, this::mapBanData, uuid.toString());
    }

    public boolean isBannedSync(UUID uuid) {
        try {
            return isBanned(uuid).get(2, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            plugin.getLogger().severe("Failed to check ban status for " + uuid + ": " + e.getMessage());
            return false;
        }
    }

    private BanData mapBanData(ResultSet rs) throws SQLException {
        if (!rs.next()) return null;
        return new BanData(
                UUID.fromString(rs.getString("uuid")),
                rs.getString("name"),
                rs.getString("reason"),
                rs.getString("banned_by"),
                rs.getLong("banned_until"),
                rs.getString("server"),
                rs.getString("discord")
        );
    }

    public CompletableFuture<Optional<UUID>> getUUIDFromName(String name) {
        String sql = "SELECT uuid FROM bans WHERE LOWER(name) = LOWER(?)";

        return dbManager.executeQuery(poolKey, sql,
                rs -> rs.next() ? UUID.fromString(rs.getString("uuid")) : null,
                name
        );
    }

    public CompletableFuture<Set<String>> getAllBannedUUIDs() {
        String sql = "SELECT uuid FROM bans";

        return dbManager.executeQuery(poolKey, sql, rs -> {
            Set<String> uuids = new HashSet<>();
            while (rs.next()) uuids.add(rs.getString("uuid"));
            return uuids;
        }).thenApply(opt -> opt.orElseGet(HashSet::new));
    }

    public CompletableFuture<Optional<String>> getNameFromUUID(String uuid) {
        String sql = "SELECT name FROM bans WHERE uuid = ?";

        return dbManager.executeQuery(poolKey, sql,
                rs -> rs.next() ? rs.getString("name") : null,
                uuid
        );
    }

    public CompletableFuture<Optional<String>> getReason(UUID uuid) {
        String sql = "SELECT reason FROM bans WHERE uuid = ?";

        return dbManager.executeQuery(poolKey, sql,
                rs -> rs.next() ? rs.getString("reason") : null,
                uuid.toString()
        );
    }
    public CompletableFuture<Optional<Long>> getUntil(UUID uuid) {
        String sql = "SELECT banned_until FROM bans WHERE uuid = ?";

        return dbManager.executeQuery(poolKey, sql,
                rs -> rs.next() ? rs.getLong("banned_until") : null,
                uuid.toString()
        );
    }

    public Optional<BanData> getBanDataSync(UUID uuid) {
        try {
            return getBanData(uuid).get(2, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            plugin.getLogger().severe("Failed to retrieve ban data for " + uuid + ": " + e.getMessage());
            return Optional.empty();
        }
    }



    public record BanData(
            UUID uuid, String name, String reason,
            String bannedBy, long until, String server, String discord
    ) {}
}
