package net.lunark.io.notes;

import net.lunark.io.database.DatabaseManager;
import org.bukkit.plugin.Plugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class NotesStorage {
    private final DatabaseManager dbManager;
    private final String poolKey = "notes";
    private final Plugin plugin;

    public NotesStorage(Plugin plugin, DatabaseManager dbManager) {
        this.plugin = plugin;
        this.dbManager = dbManager;
        initTable();
    }



    private void initTable() {
        String sql = "CREATE TABLE IF NOT EXISTS player_notes (" +
                "player_uuid TEXT NOT NULL, note_name TEXT NOT NULL, content TEXT, " +
                "created_at BIGINT, updated_at BIGINT, PRIMARY KEY (player_uuid, note_name))";
        dbManager.executeUpdate(poolKey, sql);
    }

    public CompletableFuture<Map<String, String>> getNotes(UUID playerId) {
        String sql = "SELECT note_name, content FROM player_notes WHERE player_uuid = ?";
        return dbManager.executeQuery(poolKey, sql, rs -> {
            Map<String, String> notes = new ConcurrentHashMap<>();
            while (rs.next()) {
                notes.put(rs.getString("note_name"), rs.getString("content"));
            }
            return notes;
        }, playerId.toString()).thenApply(opt -> opt.orElse(new HashMap<>()));
    }

    public CompletableFuture<Optional<String>> getNote(UUID playerId, String noteName) {
        String sql = "SELECT content FROM player_notes WHERE player_uuid = ? AND note_name = ?";
        return dbManager.executeQuery(poolKey, sql,
                rs -> rs.next() ? rs.getString("content") : null,
                playerId.toString(), noteName.toLowerCase());
    }

    public CompletableFuture<Void> saveNote(UUID playerId, String noteName, String content) {
        String sql = "INSERT OR REPLACE INTO player_notes VALUES (?, ?, ?, ?, ?)";
        long now = System.currentTimeMillis();
        return dbManager.executeUpdate(poolKey, sql,
                playerId.toString(), noteName.toLowerCase(), content, now, now);
    }
}