package net.godlycow.org.rtp.storage;

import net.godlycow.org.database.DatabaseManager;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RtpLocationStorage {
    private final DatabaseManager dbManager;
    private final String poolKey;
    private final Plugin plugin;

    public RtpLocationStorage(Plugin plugin, DatabaseManager dbManager, String poolKey) {
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
                CREATE TABLE IF NOT EXISTS rtp_locations (
                    player_uuid VARCHAR(36) PRIMARY KEY,
                    player_name VARCHAR(36),
                    world VARCHAR(64),
                    x DOUBLE,
                    y DOUBLE,
                    z DOUBLE,
                    timestamp BIGINT
                )
                """)
                .orElse("""
                CREATE TABLE IF NOT EXISTS rtp_locations (
                    player_uuid TEXT PRIMARY KEY,
                    player_name TEXT,
                    world TEXT,
                    x REAL,
                    y REAL,
                    z REAL,
                    timestamp INTEGER
                )
                """);

        dbManager.executeUpdate(poolKey, sql).exceptionally(ex -> {
            plugin.getLogger().severe("Failed to create RTP table: " + ex.getMessage());
            return null;
        });
    }

    public CompletableFuture<Void> saveRtpLocation(UUID playerId, String playerName, Location loc) {
        String sql = dbManager.getPoolKeys().stream()
                .filter(key -> key.toLowerCase().contains("mysql"))
                .findFirst()
                .map(key -> """
                INSERT INTO rtp_locations (player_uuid, player_name, world, x, y, z, timestamp)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    player_name=VALUES(player_name),
                    world=VALUES(world),
                    x=VALUES(x),
                    y=VALUES(y),
                    z=VALUES(z),
                    timestamp=VALUES(timestamp)
                """)
                .orElse("INSERT OR REPLACE INTO rtp_locations VALUES (?, ?, ?, ?, ?, ?, ?)");

        return dbManager.executeUpdate(poolKey, sql,
                playerId.toString(),
                playerName,
                loc.getWorld().getName(),
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                System.currentTimeMillis()
        );
    }
}