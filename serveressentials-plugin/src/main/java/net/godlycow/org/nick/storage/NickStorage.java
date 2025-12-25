package net.godlycow.org.nick.storage;

import net.godlycow.org.database.DatabaseManager;
import org.bukkit.plugin.Plugin;

import java.sql.ResultSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class NickStorage {
    private final Plugin plugin;
    private final DatabaseManager databaseManager;
    private final String poolKey;

    public NickStorage(Plugin plugin, DatabaseManager databaseManager, String poolKey) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.poolKey = poolKey;
        createTables();
    }

    private void createTables() {
        databaseManager.executeUpdate(poolKey, """
            CREATE TABLE IF NOT EXISTS nick_data (
                player_uuid TEXT PRIMARY KEY,
                nickname TEXT NOT NULL,
                last_change INTEGER DEFAULT (strftime('%s', 'now') * 1000),
                changes_today INTEGER DEFAULT 0
            )
        """).exceptionally(ex -> {
            plugin.getLogger().severe("Failed to create nick tables: " + ex.getMessage());
            return null;
        });
    }

    public CompletableFuture<Optional<String>> getNickname(UUID playerId) {
        return databaseManager.executeQuery(
                poolKey,
                "SELECT nickname FROM nick_data WHERE player_uuid = ?",
                (ResultSet rs) -> {
                    if (rs.next()) {
                        return rs.getString("nickname");
                    }
                    return null;
                },
                playerId.toString()
        );
    }

    public CompletableFuture<Void> setNickname(UUID playerId, String nickname) {
        return databaseManager.executeUpdate(
                poolKey,
                "INSERT OR REPLACE INTO nick_data (player_uuid, nickname, last_change) VALUES (?, ?, ?)",
                playerId.toString(),
                nickname,
                System.currentTimeMillis()
        );
    }

    public CompletableFuture<Void> removeNickname(UUID playerId) {
        return databaseManager.executeUpdate(
                poolKey,
                "DELETE FROM nick_data WHERE player_uuid = ?",
                playerId.toString()
        );
    }

    public CompletableFuture<Optional<Integer>> getDailyChanges(UUID playerId, String today) {
        return databaseManager.executeQuery(
                poolKey,
                "SELECT changes_today FROM nick_data WHERE player_uuid = ? AND DATE(last_change/1000, 'unixepoch') = ?",
                (ResultSet rs) -> {
                    if (rs.next()) {
                        return rs.getInt("changes_today");
                    }
                    return 0;
                },
                playerId.toString(),
                today
        );
    }

    public CompletableFuture<Void> incrementDailyChanges(UUID playerId) {
        return databaseManager.executeUpdate(
                poolKey,
                "UPDATE nick_data SET changes_today = changes_today + 1 WHERE player_uuid = ?",
                playerId.toString()
        );
    }

    public CompletableFuture<Void> resetDailyChanges() {
        return databaseManager.executeUpdate(
                poolKey,
                "UPDATE nick_data SET changes_today = 0 WHERE DATE(last_change/1000, 'unixepoch') < DATE('now')"
        );
    }
}