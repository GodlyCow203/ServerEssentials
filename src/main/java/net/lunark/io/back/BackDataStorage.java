package net.lunark.io.back;

import net.lunark.io.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


public class BackDataStorage {
    private final DatabaseManager dbManager;
    private final String poolKey = "back";
    private final Plugin plugin;

    public BackDataStorage(Plugin plugin, DatabaseManager dbManager) {
        this.plugin = plugin;
        this.dbManager = dbManager;
        initTable();
    }

    private void initTable() {
        String sql = "CREATE TABLE IF NOT EXISTS back_locations (" +
                "player_uuid TEXT PRIMARY KEY, " +
                "world TEXT NOT NULL, " +
                "x REAL NOT NULL, " +
                "y REAL NOT NULL, " +
                "z REAL NOT NULL, " +
                "yaw REAL NOT NULL, " +
                "pitch REAL NOT NULL, " +
                "timestamp BIGINT NOT NULL)";

        dbManager.executeUpdate(poolKey, sql).join();
        plugin.getLogger().info("Back locations table initialized (or already exists)");
    }

    public CompletableFuture<Void> saveBackLocation(UUID uuid, Location location) {
        String sql = "INSERT OR REPLACE INTO back_locations VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        return dbManager.executeUpdate(poolKey, sql,
                uuid.toString(),
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch(),
                System.currentTimeMillis()
        );
    }


    public CompletableFuture<Optional<Location>> loadBackLocation(UUID uuid) {
        String sql = "SELECT world, x, y, z, yaw, pitch FROM back_locations WHERE player_uuid = ?";
        return dbManager.executeQuery(poolKey, sql, rs -> {
            if (rs.next()) {
                String worldName = rs.getString("world");
                double x = rs.getDouble("x");
                double y = rs.getDouble("y");
                double z = rs.getDouble("z");
                float yaw = rs.getFloat("yaw");
                float pitch = rs.getFloat("pitch");

                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    return new Location(world, x, y, z, yaw, pitch);
                }
            }
            return null;
        }, uuid.toString());
    }


    public CompletableFuture<Void> deleteBackLocation(UUID uuid) {
        String sql = "DELETE FROM back_locations WHERE player_uuid = ?";
        return dbManager.executeUpdate(poolKey, sql, uuid.toString());
    }


    public CompletableFuture<Boolean> hasBackLocation(UUID uuid) {
        return loadBackLocation(uuid).thenApply(Optional::isPresent);
    }
}