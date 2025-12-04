package net.lunark.io.mail;

import net.lunark.io.database.DatabaseManager;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MailStorage {
    private final DatabaseManager dbManager;
    private final String poolKey = "mail";
    private final Plugin plugin;

    public MailStorage(Plugin plugin, DatabaseManager dbManager) {
        this.plugin = plugin;
        this.dbManager = dbManager;
        initTable();
    }

    private void initTable() {
        String tableSql = "CREATE TABLE IF NOT EXISTS mail_messages (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "target_uuid TEXT NOT NULL, " +
                "sender_uuid TEXT, " +
                "sender_name TEXT, " +
                "message TEXT, " +
                "timestamp BIGINT, " +
                "read BOOLEAN DEFAULT FALSE)";

        dbManager.executeUpdate(poolKey, tableSql)
                .thenRun(() -> {
                    String indexSql = "CREATE INDEX IF NOT EXISTS idx_target ON mail_messages(target_uuid)";
                    dbManager.executeUpdate(poolKey, indexSql).exceptionally(ex -> {
                        plugin.getLogger().warning("Failed to create mail index: " + ex.getMessage());
                        return null;
                    });

                    String cooldownTableSql = "CREATE TABLE IF NOT EXISTS mail_cooldowns (" +
                            "player_uuid TEXT PRIMARY KEY, " +
                            "timestamp BIGINT)";
                    dbManager.executeUpdate(poolKey, cooldownTableSql).exceptionally(ex -> {
                        plugin.getLogger().warning("Failed to create mail cooldown table: " + ex.getMessage());
                        return null;
                    });
                })
                .exceptionally(ex -> {
                    plugin.getLogger().severe("Failed to create mail_messages table: " + ex.getMessage());
                    return null;
                });
    }

    public CompletableFuture<List<MailMessage>> getMailbox(UUID playerId) {
        String sql = "SELECT * FROM mail_messages WHERE target_uuid = ? ORDER BY timestamp ASC";
        return dbManager.executeQuery(poolKey, sql, rs -> {
            List<MailMessage> mails = new ArrayList<>();
            while (rs.next()) {
                mails.add(new MailMessage(
                        UUID.fromString(rs.getString("sender_uuid")),
                        rs.getString("sender_name"),
                        rs.getString("message"),
                        rs.getLong("timestamp")
                ));
            }
            return mails;
        }, playerId.toString()).thenApply(opt -> opt.orElse(new ArrayList<>()));
    }

    public CompletableFuture<Void> addMail(UUID targetId, MailMessage mail) {
        String sql = "INSERT INTO mail_messages (target_uuid, sender_uuid, sender_name, message, timestamp) " +
                "VALUES (?, ?, ?, ?, ?)";
        return dbManager.executeUpdate(poolKey, sql,
                targetId.toString(),
                mail.senderId().toString(),
                mail.senderName(),
                mail.message(),
                mail.timestamp()
        );
    }

    public CompletableFuture<Void> markAllAsRead(UUID playerId) {
        String sql = "UPDATE mail_messages SET read = TRUE WHERE target_uuid = ? AND read = FALSE";
        return dbManager.executeUpdate(poolKey, sql, playerId.toString());
    }

    public CompletableFuture<Void> clearMailbox(UUID playerId) {
        String sql = "DELETE FROM mail_messages WHERE target_uuid = ?";
        return dbManager.executeUpdate(poolKey, sql, playerId.toString());
    }

    public CompletableFuture<Integer> getUnreadCount(UUID playerId) {
        String sql = "SELECT COUNT(*) as count FROM mail_messages WHERE target_uuid = ? AND read = FALSE";
        return dbManager.executeQuery(poolKey, sql, rs ->
                        rs.next() ? rs.getInt("count") : 0,
                playerId.toString()
        ).thenApply(opt -> opt.orElse(0));
    }

    public CompletableFuture<Integer> getTotalMailCount(UUID playerId) {
        String sql = "SELECT COUNT(*) as count FROM mail_messages WHERE target_uuid = ?";
        return dbManager.executeQuery(poolKey, sql, rs ->
                        rs.next() ? rs.getInt("count") : 0,
                playerId.toString()
        ).thenApply(opt -> opt.orElse(0));
    }

    public CompletableFuture<Void> saveCooldown(UUID playerId, long timestamp) {
        String sql = "INSERT OR REPLACE INTO mail_cooldowns (player_uuid, timestamp) VALUES (?, ?)";
        return dbManager.executeUpdate(poolKey, sql, playerId.toString(), timestamp);
    }

    public CompletableFuture<Long> getCooldown(UUID playerId) {
        String sql = "SELECT timestamp FROM mail_cooldowns WHERE player_uuid = ?";
        return dbManager.executeQuery(poolKey, sql, rs -> rs.next() ? rs.getLong("timestamp") : 0L,
                playerId.toString()).thenApply(opt -> opt.orElse(0L));
    }
}