package net.lunark.io.warp;

import net.lunark.io.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class WarpStorage {
    private final DatabaseManager dbManager;
    private final String poolKey = "warps";
    private final Plugin plugin;

    public WarpStorage(Plugin plugin, DatabaseManager dbManager) {
        this.plugin = plugin;
        this.dbManager = dbManager;
        initTable();
    }

    private void initTable() {
        String sql = "CREATE TABLE IF NOT EXISTS warps (" +
                "name TEXT PRIMARY KEY, " +
                "world TEXT NOT NULL, " +
                "x REAL NOT NULL, " +
                "y REAL NOT NULL, " +
                "z REAL NOT NULL, " +
                "yaw REAL NOT NULL, " +
                "pitch REAL NOT NULL, " +
                "creator_uuid TEXT NOT NULL)";
        dbManager.executeUpdate(poolKey, sql);
    }

    public CompletableFuture<Void> addWarp(String name, Location loc, UUID creator) {
        String sql = "INSERT OR REPLACE INTO warps VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        return dbManager.executeUpdate(poolKey, sql,
                name.toLowerCase(),
                loc.getWorld().getName(),
                loc.getX(), loc.getY(), loc.getZ(),
                loc.getYaw(), loc.getPitch(),
                creator.toString());
    }

    public CompletableFuture<Void> removeWarp(String name) {
        String sql = "DELETE FROM warps WHERE name = ?";
        return dbManager.executeUpdate(poolKey, sql, name.toLowerCase());
    }

    public CompletableFuture<Optional<Optional<Location>>> getWarp(String name) {
        String sql = "SELECT * FROM warps WHERE name = ?";
        return dbManager.executeQuery(poolKey, sql, this::mapLocation, name.toLowerCase());
    }

    public CompletableFuture<Optional<UUID>> getCreator(String name) {
        String sql = "SELECT creator_uuid FROM warps WHERE name = ?";
        return dbManager.executeQuery(poolKey, sql,
                rs -> rs.next() ? UUID.fromString(rs.getString("creator_uuid")) : null,
                name.toLowerCase());
    }

    public CompletableFuture<Map<String, Location>> getAllWarps() {
        String sql = "SELECT * FROM warps";

        return dbManager.executeQuery(poolKey, sql, rs -> {
                    Map<String, Location> warps = new HashMap<>();

                    while (rs.next()) {
                        String name = rs.getString("name");
                        String worldName = rs.getString("world");
                        if (worldName == null) continue;

                        World world = Bukkit.getWorld(worldName);
                        if (world == null) continue;

                        Location loc = new Location(
                                world,
                                rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
                                (float) rs.getDouble("yaw"), (float) rs.getDouble("pitch")
                        );

                        warps.put(name, loc);
                    }

                    return warps;
                })
                .thenApply(opt -> opt.orElseGet(HashMap::new));
    }


    public CompletableFuture<Boolean> exists(String name) {
        String sql = "SELECT 1 FROM warps WHERE name = ?";
        return dbManager.executeQuery(poolKey, sql,
                        rs -> rs.next(),
                        name.toLowerCase())
                .thenApply(opt -> opt.orElse(false));
    }

    public CompletableFuture<Long> getWarpCountByCreator(UUID creator) {
        String sql = "SELECT COUNT(*) as count FROM warps WHERE creator_uuid = ?";
        return dbManager.executeQuery(poolKey, sql,
                        rs -> rs.next() ? rs.getLong("count") : 0L,
                        creator.toString())
                .thenApply(opt -> opt.orElse(0L));
    }

    private Optional<Location> mapLocation(ResultSet rs) throws SQLException {
        if (!rs.next()) return Optional.empty();
        String worldName = rs.getString("world");
        if (worldName == null) return Optional.empty();
        World world = Bukkit.getWorld(worldName);
        if (world == null) return Optional.empty();
        return Optional.of(new Location(
                world,
                rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
                (float)rs.getDouble("yaw"), (float)rs.getDouble("pitch")
        ));
    }

    private Optional<Map<String, Location>> mapAllWarps(ResultSet rs) throws SQLException {
        Map<String, Location> warps = new HashMap<>();
        while (rs.next()) {
            String name = rs.getString("name");
            String worldName = rs.getString("world");
            if (worldName == null) continue;
            World world = Bukkit.getWorld(worldName);
            if (world == null) continue;
            Location loc = new Location(
                    world,
                    rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
                    (float)rs.getDouble("yaw"), (float)rs.getDouble("pitch")
            );
            warps.put(name, loc);
        }
        return Optional.of(warps);
    }
}