package net.godlycow.org.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.godlycow.org.database.type.DatabaseType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class DatabaseManager {
    private final Plugin plugin;
    private final Map<String, HikariDataSource> pools = new ConcurrentHashMap<>();
    private final Map<String, DatabaseStatus> statusMap = new ConcurrentHashMap<>();


    public record DatabaseStatus(boolean connected, String message, Instant lastCheck) {}

    public DatabaseManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void initializePool(String key, DatabaseConfig config) {
        try {
            plugin.getLogger().info("Initializing database pool: " + key + " (" + config.type() + ")");

            HikariConfig hc = new HikariConfig();

            if (config.type() == DatabaseType.SQLITE) {
                String path = plugin.getDataFolder().getAbsolutePath() + "/" + config.sqliteFile();
                hc.setJdbcUrl("jdbc:sqlite:" + path);
                hc.setDriverClassName("org.sqlite.JDBC");
                hc.addDataSourceProperty("journal_mode", "WAL");
                hc.addDataSourceProperty("synchronous", "NORMAL");
                plugin.getLogger().info("SQLite path: " + path);
            } else {
                // MySQL with enhanced connection settings
                String jdbc = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&connectTimeout=5000&socketTimeout=30000",
                        config.mysqlHost(), config.mysqlPort(), config.mysqlDatabase());
                hc.setJdbcUrl(jdbc);
                hc.setUsername(config.mysqlUser());
                hc.setPassword(config.mysqlPassword());
                hc.setDriverClassName("com.mysql.cj.jdbc.Driver");

                // Optimized pool settings
                hc.setMaximumPoolSize(config.poolSize());
                hc.setMinimumIdle(Math.min(1, config.poolSize() - 1));
                hc.setConnectionTimeout(5000); // 5 second timeout
                hc.setIdleTimeout(600000); // 10 minutes
                hc.setMaxLifetime(1800000); // 30 minutes
                hc.setLeakDetectionThreshold(60000); // 1 minute

                plugin.getLogger().info("MySQL connection: " + config.mysqlHost() + ":" + config.mysqlPort());
            }

            HikariDataSource ds = new HikariDataSource(hc);

            // Test connection before marking as connected
            try (Connection conn = ds.getConnection()) {
                if (conn.isValid(3)) {
                    pools.put(key, ds);
                    statusMap.put(key, new DatabaseStatus(true, "Connected successfully", Instant.now()));
                } else {
                    ds.close();
                    throw new SQLException("Connection validation failed");
                }
            }

        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize database pool: " + key, ex);
            statusMap.put(key, new DatabaseStatus(false, ex.getMessage(), Instant.now()));

            // Show MySQL troubleshooting if applicable
            if (config.type() == DatabaseType.MYSQL) {
                diagnoseMySQL(config);
            }

            plugin.getLogger().warning("Plugin will continue without this database. Features will be limited.");
        }
    }

    private void diagnoseMySQL(DatabaseConfig config) {
        plugin.getLogger().warning("╔═══════════════════════════════════════════════════════╗");
        plugin.getLogger().warning("║        MySQL Connection Failed - Diagnostics          ║");
        plugin.getLogger().warning("╚═══════════════════════════════════════════════════════╝");
        plugin.getLogger().warning("  Host: " + config.mysqlHost() + ":" + config.mysqlPort());
        plugin.getLogger().warning("  Database: " + config.mysqlDatabase());
        plugin.getLogger().warning("  User: " + config.mysqlUser());
        plugin.getLogger().warning("║ SOLUTIONS:");
        plugin.getLogger().warning("║ 1. Verify MySQL is running: sudo systemctl status mysql");
        plugin.getLogger().warning("║ 2. Test manually: mysql -u " + config.mysqlUser() + " -p -h " + config.mysqlHost());
        plugin.getLogger().warning("║ 3. Check firewall: sudo ufw allow 3306");
        plugin.getLogger().warning("║ 4. Check bind-address in my.cnf (should be 0.0.0.0)");
        plugin.getLogger().warning("║ 5. Grant remote access: GRANT ALL ON " + config.mysqlDatabase() + ".* TO '" + config.mysqlUser() + "'@'%';");
        plugin.getLogger().warning("║ 6. Try SQLite instead if you don't need remote DB!");
    }

    public CompletableFuture<Void> executeUpdate(String poolKey, String sql, Object... params) {
        return CompletableFuture.runAsync(() -> {
            HikariDataSource ds = pools.get(poolKey);
            if (ds == null) throw new IllegalStateException("No such database pool: " + poolKey);

            try (Connection conn = ds.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
                ps.executeUpdate();
                statusMap.put(poolKey, new DatabaseStatus(true, "OK", Instant.now()));

            } catch (SQLException e) {
                statusMap.put(poolKey, new DatabaseStatus(false, e.getMessage(), Instant.now()));
                plugin.getLogger().log(Level.SEVERE, "SQL Error in pool '" + poolKey + "': " + sql, e);
                throw new CompletionException(e);
            }
        });
    }

    public <T> CompletableFuture<Optional<T>> executeQuery(String poolKey, String sql, ResultMapper<T> mapper, Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            HikariDataSource ds = pools.get(poolKey);
            if (ds == null) throw new IllegalStateException("No such database pool: " + poolKey);

            try (Connection conn = ds.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    return Optional.ofNullable(mapper.map(rs));
                }
            } catch (Exception e) {
                statusMap.put(poolKey, new DatabaseStatus(false, e.getMessage(), Instant.now()));
                plugin.getLogger().log(Level.SEVERE, "SQL Query Error in pool '" + poolKey + "': " + sql, e);
                throw new CompletionException(e);
            }
        });
    }

    public Map<String, DatabaseStatus> getAllStatus() {
        return new HashMap<>(statusMap);
    }

    public boolean isPoolConnected(String key) {
        DatabaseStatus status = statusMap.get(key);
        if (status == null) return false;
        if (status.connected()) {
            // Verify connection is still alive
            return testConnection(key);
        }
        return false;
    }

    private boolean testConnection(String key) {
        try {
            HikariDataSource ds = pools.get(key);
            if (ds == null) return false;
            try (Connection conn = ds.getConnection()) {
                return conn.isValid(2);
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public void closePool(String key) {
        HikariDataSource ds = pools.remove(key);
        if (ds != null) {
            ds.close();
            plugin.getLogger().info("Closed database pool: " + key);
        }
    }

    public void closeAll() {
        new HashMap<>(pools).forEach((key, ds) -> {
            try {
                ds.close();
                plugin.getLogger().info("Closed database pool: " + key);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error closing pool: " + key, e);
            }
        });
        pools.clear();
        statusMap.clear();
    }

    public Set<String> getPoolKeys() {
        return Collections.unmodifiableSet(pools.keySet());
    }

    @FunctionalInterface
    public interface ResultMapper<T> {
        @Nullable T map(ResultSet rs) throws Exception;
    }
}