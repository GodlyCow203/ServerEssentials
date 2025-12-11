package net.lunark.io.lobby;

import net.lunark.io.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class LobbyStorage {
    private final DatabaseManager dbManager;
    private final String poolKey = "lobby";
    private final Plugin plugin;

    public LobbyStorage(Plugin plugin, DatabaseManager dbManager) {
        this.plugin = plugin;
        this.dbManager = dbManager;
        initTable();
    }

    private void initTable() {
        String sql = "CREATE TABLE IF NOT EXISTS lobby_locations (" +
                "world TEXT PRIMARY KEY, " +
                "location_data TEXT NOT NULL)";
        dbManager.executeUpdate(poolKey, sql);
    }

    public CompletableFuture<Void> setLobby(Location location) {
        return setWorldLobby("global", location);
    }

    public CompletableFuture<Void> setWorldLobby(String world, Location location) {
        String sql = "INSERT OR REPLACE INTO lobby_locations VALUES (?, ?)";
        String serialized = serializeLocation(location);
        return dbManager.executeUpdate(poolKey, sql, world, serialized);
    }

    public CompletableFuture<Optional<Location>> getLobby(String world) {
        if (world == null) {
            return getGlobalLobby();
        }

        return getWorldLobby(world).thenCompose(opt -> {
            if (opt.isPresent()) {
                return CompletableFuture.completedFuture(opt);
            }
            return getGlobalLobby();
        });
    }

    private CompletableFuture<Optional<Location>> getGlobalLobby() {
        return getWorldLobby("global");
    }

    private CompletableFuture<Optional<Location>> getWorldLobby(String world) {
        String sql = "SELECT location_data FROM lobby_locations WHERE world = ?";
        return dbManager.executeQuery(poolKey, sql,
                rs -> rs.next() ? deserializeLocation(rs.getString("location_data")) : null,
                world);
    }

    public CompletableFuture<Void> removeLobby(String world) {
        if (world == null) {
            return removeGlobalLobby();
        }
        String sql = "DELETE FROM lobby_locations WHERE world = ?";
        return dbManager.executeUpdate(poolKey, sql, world);
    }

    private CompletableFuture<Void> removeGlobalLobby() {
        String sql = "DELETE FROM lobby_locations WHERE world = ?";
        return dbManager.executeUpdate(poolKey, sql, "global");
    }

    public CompletableFuture<Boolean> hasLobby(String world) {
        return getLobby(world).thenApply(Optional::isPresent);
    }

    private String serializeLocation(Location loc) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             BukkitObjectOutputStream boos = new BukkitObjectOutputStream(baos)) {
            boos.writeObject(loc);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to serialize location: " + e.getMessage());
            return "";
        }
    }

    private Location deserializeLocation(String data) {
        if (data == null || data.isEmpty()) return null;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(data));
             BukkitObjectInputStream bois = new BukkitObjectInputStream(bais)) {
            return (Location) bois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            plugin.getLogger().severe("Failed to deserialize location: " + e.getMessage());
            return null;
        }
    }
}