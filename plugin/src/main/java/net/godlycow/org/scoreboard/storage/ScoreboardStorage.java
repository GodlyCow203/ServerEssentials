package net.godlycow.org.scoreboard.storage;

import net.godlycow.org.database.DatabaseManager;
import org.bukkit.plugin.Plugin;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;


public final class ScoreboardStorage {
    private final DatabaseManager dbManager;
    private final String poolKey = "scoreboard";
    private final Map<UUID, ScoreboardPlayerData> cache = new ConcurrentHashMap<>();
    private final AtomicInteger enabledCount = new AtomicInteger(0);

    public record ScoreboardPlayerData(boolean enabled, String layout, long lastUpdate, long joinTime) {}

    public ScoreboardStorage(Plugin plugin, DatabaseManager dbManager) {
        this.dbManager = dbManager;
        initTable();
        plugin.getLogger().info("Scoreboard storage initialized with async support");
    }


    private void initTable() {
        String sql = "CREATE TABLE IF NOT EXISTS scoreboard_players (" +
                "player_uuid TEXT PRIMARY KEY, enabled BOOLEAN, layout TEXT, last_update BIGINT, join_time BIGINT)";
        dbManager.executeUpdate(poolKey, sql);
    }

    public CompletableFuture<ScoreboardPlayerData> loadPlayer(UUID playerId) {
        String sql = "SELECT enabled, layout, last_update, join_time FROM scoreboard_players WHERE player_uuid = ?";
        return dbManager.executeQuery(poolKey, sql, rs -> {
            if (rs.next()) {
                return new ScoreboardPlayerData(
                        rs.getBoolean("enabled"),
                        rs.getString("layout"),
                        rs.getLong("last_update"),
                        rs.getLong("join_time")
                );
            }
            return null;
        }, playerId.toString()).thenApply(opt -> {
            ScoreboardPlayerData data = opt.orElse(new ScoreboardPlayerData(true, null, 0, System.currentTimeMillis()));
            cache.put(playerId, data);
            if (data.enabled()) enabledCount.incrementAndGet();
            return data;
        });
    }

    public CompletableFuture<Void> savePlayer(UUID playerId, boolean enabled, String layout) {
        long now = System.currentTimeMillis();
        long joinTime = cache.containsKey(playerId) ? cache.get(playerId).joinTime() : now;

        cache.put(playerId, new ScoreboardPlayerData(enabled, layout, now, joinTime));

        String sql = "INSERT OR REPLACE INTO scoreboard_players VALUES (?, ?, ?, ?, ?)";
        return dbManager.executeUpdate(poolKey, sql,
                playerId.toString(), enabled, layout, now, joinTime);
    }

    public CompletableFuture<Void> setEnabled(UUID playerId, boolean enabled) {
        ScoreboardPlayerData current = cache.getOrDefault(playerId,
                new ScoreboardPlayerData(true, null, 0, System.currentTimeMillis()));

        if (current.enabled() != enabled) {
            if (enabled) {
                enabledCount.incrementAndGet();
            } else {
                enabledCount.decrementAndGet();
            }
        }

        return savePlayer(playerId, enabled, current.layout());
    }

    public CompletableFuture<Void> setLayout(UUID playerId, String layout) {
        ScoreboardPlayerData current = cache.getOrDefault(playerId,
                new ScoreboardPlayerData(true, null, 0, System.currentTimeMillis()));
        return savePlayer(playerId, current.enabled(), layout);
    }

    public boolean isEnabled(UUID playerId) {
        return cache.getOrDefault(playerId, new ScoreboardPlayerData(true, null, 0, 0)).enabled();
    }

    public String getLayout(UUID playerId) {
        return cache.getOrDefault(playerId, new ScoreboardPlayerData(true, null, 0, 0)).layout();
    }

    public void removeFromCache(UUID playerId) {
        ScoreboardPlayerData data = cache.remove(playerId);
        if (data != null && data.enabled()) {
            enabledCount.decrementAndGet();
        }
    }

    public int getEnabledPlayerCount() {
        return enabledCount.get();
    }

    public Map<UUID, ScoreboardPlayerData> getCacheSnapshot() {
        return Map.copyOf(cache);
    }
}