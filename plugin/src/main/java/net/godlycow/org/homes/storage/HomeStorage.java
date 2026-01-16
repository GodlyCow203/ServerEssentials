package net.godlycow.org.homes.storage;

import net.godlycow.org.database.DatabaseManager;
import net.godlycow.org.homes.model.Home;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Bukkit;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class HomeStorage {
    private final DatabaseManager dbManager;
    private final String poolKey = "homes";

    public HomeStorage(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        initTable();
    }

    private void initTable() {
        String sql = "CREATE TABLE IF NOT EXISTS homes (" +
                "player_uuid TEXT NOT NULL, " +
                "slot INTEGER NOT NULL, " +
                "name TEXT NOT NULL, " +
                "world TEXT NOT NULL, " +
                "x REAL NOT NULL, " +
                "y REAL NOT NULL, " +
                "z REAL NOT NULL, " +
                "yaw REAL NOT NULL, " +
                "pitch REAL NOT NULL, " +
                "PRIMARY KEY (player_uuid, slot))";

        dbManager.executeUpdate(poolKey, sql)
                .exceptionally(ex -> {
                    Bukkit.getLogger().log(Level.SEVERE, "Failed to create homes table", ex);
                    return null;
                });
    }

    public CompletableFuture<Void> setHome(UUID playerId, int slot, Home home) {
        String sql = "INSERT OR REPLACE INTO homes VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Location loc = home.toLocation();

        if (loc == null || loc.getWorld() == null) {
            Bukkit.getLogger().warning("Attempted to save home with null location for " + playerId);
            return CompletableFuture.completedFuture(null);
        }

        return dbManager.executeUpdate(poolKey, sql,
                playerId.toString(),
                slot,
                home.getName(),
                loc.getWorld().getName(),
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                loc.getYaw(),
                loc.getPitch()
        ).exceptionally(ex -> {
            Bukkit.getLogger().log(Level.WARNING, "Failed to save home for " + playerId, ex);
            return null;
        });
    }

    public CompletableFuture<Optional<Home>> getHome(UUID playerId, int slot) {
        String sql = "SELECT * FROM homes WHERE player_uuid = ? AND slot = ?";
        return dbManager.executeQuery(poolKey, sql,
                rs -> {
                    if (rs.next()) {
                        Home home = mapHome(rs);
                        return home != null ? home : null;
                    }
                    return null;
                },
                playerId.toString(),
                slot
        ).exceptionally(ex -> {
            Bukkit.getLogger().log(Level.WARNING, "Failed to fetch home for " + playerId, ex);
            return Optional.empty();
        });
    }

    public CompletableFuture<Void> removeHome(UUID playerId, int slot) {
        String sql = "DELETE FROM homes WHERE player_uuid = ? AND slot = ?";
        return dbManager.executeUpdate(poolKey, sql, playerId.toString(), slot)
                .exceptionally(ex -> {
                    Bukkit.getLogger().log(Level.WARNING, "Failed to remove home for " + playerId, ex);
                    return null;
                });
    }

    public CompletableFuture<Map<Integer, Home>> getAllHomes(UUID playerId) {
        String sql = "SELECT * FROM homes WHERE player_uuid = ?";
        return dbManager.executeQuery(poolKey, sql,
                        rs -> {
                            Map<Integer, Home> homes = new HashMap<>();
                            while (rs.next()) {
                                Home home = mapHome(rs);
                                if (home != null) {
                                    homes.put(rs.getInt("slot"), home);
                                }
                            }
                            return homes;
                        },
                        playerId.toString()
                ).thenApply(opt -> opt.orElse(new HashMap<>()))
                .exceptionally(ex -> {
                    Bukkit.getLogger().log(Level.WARNING, "Failed to fetch homes for " + playerId, ex);
                    return new HashMap<>();
                });
    }

    private Home mapHome(ResultSet rs) throws SQLException {
        String worldName = rs.getString("world");
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            Bukkit.getLogger().warning("World '" + worldName + "' not found for home '" + rs.getString("name") + "'");
            return null;
        }

        Home home = new Home();
        home.setName(rs.getString("name"));
        home.setLocation(new Location(
                world,
                rs.getDouble("x"),
                rs.getDouble("y"),
                rs.getDouble("z"),
                rs.getFloat("yaw"),
                rs.getFloat("pitch")
        ));
        return home;
    }
}