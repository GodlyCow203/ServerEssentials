package net.godlycow.org.kit.storage;

import net.godlycow.org.database.DatabaseManager;
import org.bukkit.plugin.Plugin;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class KitStorage {
    private final Plugin plugin;
    private final DatabaseManager dbManager;
    private final String poolKey;
    private final ConcurrentHashMap<UUID, ConcurrentHashMap<String, KitClaimData>> cache = new ConcurrentHashMap<>();

    public record KitClaimData(long lastClaimed, int claimCount) {}

    public KitStorage(Plugin plugin, DatabaseManager dbManager, String poolKey) {
        this.plugin = plugin;
        this.dbManager = dbManager;
        this.poolKey = poolKey;
        initTable();
    }

    private void initTable() {
        String sql = dbManager.getPoolKeys().stream()
                .filter(key -> key.toLowerCase().contains("mysql"))
                .findFirst()
                .map(key -> """
                CREATE TABLE IF NOT EXISTS kit_claims (
                    player_uuid VARCHAR(36) NOT NULL,
                    kit_id VARCHAR(64) NOT NULL,
                    last_claimed BIGINT NOT NULL,
                    claim_count INTEGER DEFAULT 1,
                    PRIMARY KEY (player_uuid, kit_id)
                )
                """)
                .orElse("""
                CREATE TABLE IF NOT EXISTS kit_claims (
                    player_uuid TEXT NOT NULL,
                    kit_id TEXT NOT NULL,
                    last_claimed INTEGER NOT NULL,
                    claim_count INTEGER DEFAULT 1,
                    PRIMARY KEY (player_uuid, kit_id)
                )
                """);

        dbManager.executeUpdate(poolKey, sql).exceptionally(ex -> {
            plugin.getLogger().severe("❌ Failed to create kit_claims table: " + ex.getMessage());
            return null;
        });
    }

    public CompletableFuture<Void> saveKitClaim(UUID playerId, String kitId) {
        long now = System.currentTimeMillis();
        cache.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>())
                .put(kitId, new KitClaimData(now, getSync(playerId, kitId).claimCount + 1));

        String sql = dbManager.getPoolKeys().stream()
                .filter(key -> key.toLowerCase().contains("mysql"))
                .findFirst()
                .map(key -> """
                INSERT INTO kit_claims (player_uuid, kit_id, last_claimed, claim_count)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    last_claimed = VALUES(last_claimed),
                    claim_count = VALUES(claim_count)
                """)
                .orElse("""
                INSERT OR REPLACE INTO kit_claims VALUES (?, ?, ?, ?)
                """);

        return dbManager.executeUpdate(poolKey, sql,
                playerId.toString(),
                kitId.toLowerCase(),
                now,
                getSync(playerId, kitId).claimCount
        );
    }

    public CompletableFuture<KitClaimData> getKitClaim(UUID playerId, String kitId) {
        return dbManager.executeQuery(poolKey,
                "SELECT last_claimed, claim_count FROM kit_claims WHERE player_uuid = ? AND kit_id = ?",
                rs -> rs.next() ? new KitClaimData(rs.getLong("last_claimed"), rs.getInt("claim_count")) : null,
                playerId.toString(), kitId.toLowerCase()
        ).thenApply(opt -> opt.orElse(new KitClaimData(0, 0)));
    }

    private KitClaimData getSync(UUID playerId, String kitId) {
        return cache.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>())
                .getOrDefault(kitId, new KitClaimData(0, 0));
    }

    public boolean isOnCooldown(UUID playerId, String kitId, int cooldownSeconds) {
        KitClaimData data = getSync(playerId, kitId);
        if (data.lastClaimed() == 0) return false;

        long elapsed = (System.currentTimeMillis() - data.lastClaimed()) / 1000;
        return elapsed < cooldownSeconds;
    }

    public long getRemainingCooldown(UUID playerId, String kitId, int cooldownSeconds) {
        KitClaimData data = getSync(playerId, kitId);
        if (data.lastClaimed() == 0) return 0;

        long elapsed = (System.currentTimeMillis() - data.lastClaimed()) / 1000;
        long remaining = cooldownSeconds - elapsed;
        return Math.max(0, remaining);
    }

    public void loadIntoCache(UUID playerId) {
        dbManager.executeQuery(poolKey,
                "SELECT kit_id, last_claimed, claim_count FROM kit_claims WHERE player_uuid = ?",
                rs -> {
                    while (rs.next()) {
                        cache.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>())
                                .put(rs.getString("kit_id").toLowerCase(),
                                        new KitClaimData(rs.getLong("last_claimed"), rs.getInt("claim_count")));
                    }
                    return null;
                },
                playerId.toString()
        );
    }

    public void unloadFromCache(UUID playerId) {
        cache.remove(playerId);
    }
}