package net.lunark.io.scoreboard;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ScoreboardDatabase {
    private final HikariDataSource ds;

    public ScoreboardDatabase(JavaPlugin plugin) {
        File pluginFolder = plugin.getDataFolder();
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs();
        }

        File databaseFolder = new File(pluginFolder, "Database");
        if (!databaseFolder.exists()) {
            databaseFolder.mkdirs();
        }

        File dbFile = new File(databaseFolder, "scoreboard.db");

        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        cfg.setMaximumPoolSize(10);
        this.ds = new HikariDataSource(cfg);

        try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS players (uuid TEXT PRIMARY KEY, enabled BOOLEAN, layout TEXT)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public CompletableFuture<Void> save(UUID uuid, boolean enabled, String layout) {
        return CompletableFuture.runAsync(() -> {
            try (Connection c = ds.getConnection();
                 PreparedStatement ps = c.prepareStatement("REPLACE INTO players VALUES (?,?,?)")) {
                ps.setString(1, uuid.toString());
                ps.setBoolean(2, enabled);
                ps.setString(3, layout);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<PlayerData> load(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection c = ds.getConnection();
                 PreparedStatement ps = c.prepareStatement("SELECT * FROM players WHERE uuid=?")) {
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return new PlayerData(rs.getBoolean("enabled"), rs.getString("layout"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return new PlayerData(true, "default");
        });
    }

    public record PlayerData(boolean enabled, String layout) {}
}
