package net.lunark.io.commands;

import net.lunark.io.database.DatabaseManager;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Generic storage for command states in command_data.db
 * Table structure: command_states (player_uuid TEXT, command TEXT, key TEXT, value TEXT, timestamp BIGINT)
 */
public class CommandDataStorage {
    private final DatabaseManager dbManager;
    private final String poolKey = "command_data";
    private final Plugin plugin;

    public CommandDataStorage(Plugin plugin, DatabaseManager dbManager) {
        this.plugin = plugin;
        this.dbManager = dbManager;
        initTable();
    }

    private void initTable() {
        String sql = "CREATE TABLE IF NOT EXISTS command_states (" +
                "player_uuid TEXT NOT NULL, " +
                "command TEXT NOT NULL, " +
                "key TEXT NOT NULL, " +
                "value TEXT, " +
                "timestamp BIGINT, " +
                "PRIMARY KEY (player_uuid, command, key))";

        dbManager.executeUpdate(poolKey, sql).exceptionally(ex -> {
            plugin.getLogger().severe("Failed to create command_states table: " + ex.getMessage());
            return null;
        });
    }

    /**
     * Save a command state value
     * @param playerId Player UUID
     * @param command Command name (e.g., "fly")
     * @param key State key (e.g., "enabled")
     * @param value State value as string
     */
    public CompletableFuture<Void> setState(UUID playerId, String command, String key, String value) {
        String sql = "INSERT OR REPLACE INTO command_states (player_uuid, command, key, value, timestamp) VALUES (?, ?, ?, ?, ?)";
        return dbManager.executeUpdate(poolKey, sql,
                playerId.toString(), command, key, value, System.currentTimeMillis());
    }

    /**
     * Get a command state value
     * @return CompletableFuture with Optional<String> - empty if not found
     */
    public CompletableFuture<Optional<String>> getState(UUID playerId, String command, String key) {
        String sql = "SELECT value FROM command_states WHERE player_uuid = ? AND command = ? AND key = ?";
        return dbManager.executeQuery(poolKey, sql,
                rs -> rs.next() ? rs.getString("value") : null,
                playerId.toString(), command, key);
    }

    /**
     * Get all states for a specific command
     */
    public CompletableFuture<List<CommandState>> getAllStatesForCommand(UUID playerId, String command) {
        String sql = "SELECT key, value FROM command_states WHERE player_uuid = ? AND command = ?";
        return dbManager.executeQuery(poolKey, sql, rs -> {
            List<CommandState> states = new java.util.ArrayList<>();
            while (rs.next()) {
                states.add(new CommandState(playerId, command, rs.getString("key"), rs.getString("value")));
            }
            return states;
        }, playerId.toString(), command).thenApply(opt -> opt.orElse(List.of()));
    }

    /**
     * Delete a specific state
     */
    public CompletableFuture<Void> deleteState(UUID playerId, String command, String key) {
        String sql = "DELETE FROM command_states WHERE player_uuid = ? AND command = ? AND key = ?";
        return dbManager.executeUpdate(poolKey, sql, playerId.toString(), command, key);
    }

    /**
     * Delete all states for a command
     */
    public CompletableFuture<Void> deleteAllForCommand(UUID playerId, String command) {
        String sql = "DELETE FROM command_states WHERE player_uuid = ? AND command = ?";
        return dbManager.executeUpdate(poolKey, sql, playerId.toString(), command);
    }

    /**
     * Clear old states (for cleanup)
     * @param olderThanMillis Delete entries older than this
     */
    public CompletableFuture<Void> cleanupOldStates(long olderThanMillis) {
        String sql = "DELETE FROM command_states WHERE timestamp < ?";
        return dbManager.executeUpdate(poolKey, sql, System.currentTimeMillis() - olderThanMillis);
    }
}