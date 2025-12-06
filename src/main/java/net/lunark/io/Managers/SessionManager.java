package net.lunark.io.Managers;

import net.lunark.io.commands.CommandDataStorage;
import net.lunark.io.database.DatabaseConfig;
import net.lunark.io.database.DatabaseManager;
import net.lunark.io.database.DatabaseType;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


public class SessionManager {
    private final Map<UUID, Long> currentSessions = new HashMap<>();
    private final Map<UUID, Long> longestSessions = new HashMap<>();

    private final DatabaseManager databaseManager;
    private final CommandDataStorage dataStorage;
    private final String poolKey = "session";

    private boolean initialized = false;

    public SessionManager(DatabaseManager databaseManager, CommandDataStorage dataStorage) {
        this.databaseManager = databaseManager;
        this.dataStorage = dataStorage;
    }


    public void initialize() {
        if (initialized) return;

        DatabaseConfig config = new DatabaseConfig(
                DatabaseType.SQLITE,
                "session.db",
                null, 0, null, null, null, 5
        );
        databaseManager.initializePool(poolKey, config);

        initTable();
        initialized = true;
    }

    private void initTable() {
        String sql = "CREATE TABLE IF NOT EXISTS session_data (" +
                "player_uuid TEXT PRIMARY KEY, " +
                "longest_session_ms BIGINT NOT NULL DEFAULT 0, " +
                "last_updated BIGINT NOT NULL)";

        databaseManager.executeUpdate(poolKey, sql).join();
    }


    public void startSession(Player player) {
        currentSessions.put(player.getUniqueId(), System.currentTimeMillis());
    }


    public void endSession(Player player) {
        UUID uuid = player.getUniqueId();
        Long startTime = currentSessions.get(uuid);
        if (startTime == null) return;

        long duration = System.currentTimeMillis() - startTime;
        currentSessions.remove(uuid);

        getLongestSession(player).thenAccept(previousLongest -> {
            if (duration > previousLongest) {
                longestSessions.put(uuid, duration);
                saveLongestSession(uuid, duration);
            }
        });
    }


    public long getCurrentSession(Player player) {
        Long startTime = currentSessions.get(player.getUniqueId());
        if (startTime == null) return 0;
        return System.currentTimeMillis() - startTime;
    }

    public CompletableFuture<Long> getLongestSession(Player player) {
        UUID uuid = player.getUniqueId();

        // Return cached value if available
        if (longestSessions.containsKey(uuid)) {
            return CompletableFuture.completedFuture(longestSessions.get(uuid));
        }

        return dataStorage.getState(uuid, "session", "longest")
                .thenApply(opt -> {
                    long longest = opt.map(Long::parseLong).orElse(0L);
                    longestSessions.put(uuid, longest); // Cache it
                    return longest;
                });
    }


    private void saveLongestSession(UUID uuid, long duration) {
        dataStorage.setState(uuid, "session", "longest", String.valueOf(duration))
                .exceptionally(ex -> {
                    System.err.println("Failed to save longest session for " + uuid + ": " + ex.getMessage());
                    return null;
                });
    }
}