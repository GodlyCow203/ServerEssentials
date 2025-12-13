package net.lunark.io.vault;

import net.lunark.io.database.DatabaseManager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class VaultStorage {
    private final DatabaseManager dbManager;
    private final Plugin plugin;
    private static final String POOL_KEY = "vaults";
    private static final String TABLE = "vaults";

    public VaultStorage(Plugin plugin, DatabaseManager dbManager) {
        this.plugin = plugin;
        this.dbManager = dbManager;
        createTable();
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE + " (" +
                "uuid VARCHAR(36) NOT NULL, " +
                "vault_number INTEGER NOT NULL, " +
                "inventory_data TEXT NOT NULL, " +
                "PRIMARY KEY (uuid, vault_number))";

        dbManager.executeUpdate(POOL_KEY, sql).join();
    }

    public CompletableFuture<Void> save(UUID playerId, int vaultNumber, Inventory inventory) {
        return CompletableFuture.supplyAsync(() -> serializeInventory(inventory))
                .thenCompose(data -> {
                    String sql = "INSERT OR REPLACE INTO " + TABLE + " VALUES (?, ?, ?)";
                    return dbManager.executeUpdate(POOL_KEY, sql,
                            playerId.toString(),
                            String.valueOf(vaultNumber),
                            data);
                });
    }

    public CompletableFuture<Optional<String>> load(UUID playerId, int vaultNumber) {
        String sql = "SELECT inventory_data FROM " + TABLE + " WHERE uuid = ? AND vault_number = ?";

        return dbManager.executeQuery(
                POOL_KEY,
                sql,
                rs -> rs.next() ? rs.getString("inventory_data") : null,
                playerId.toString(),
                String.valueOf(vaultNumber)
        ).thenApply(s -> Optional.ofNullable(String.valueOf(s)));
    }


    public CompletableFuture<Void> delete(UUID playerId, int vaultNumber) {
        String sql = "DELETE FROM " + TABLE + " WHERE uuid = ? AND vault_number = ?";
        return dbManager.executeUpdate(POOL_KEY, sql,
                playerId.toString(),
                String.valueOf(vaultNumber));
    }

    public CompletableFuture<Boolean> exists(UUID playerId, int vaultNumber) {
        String sql = "SELECT 1 FROM " + TABLE + " WHERE uuid = ? AND vault_number = ?";
        return dbManager.executeQuery(POOL_KEY, sql,
                        rs -> rs.next(),
                        playerId.toString(), String.valueOf(vaultNumber))
                .thenApply(opt -> opt.orElse(false));
    }

    private String serializeInventory(Inventory inv) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             BukkitObjectOutputStream boos = new BukkitObjectOutputStream(baos)) {

            boos.writeInt(inv.getSize());
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack item = inv.getItem(i);
                boos.writeObject(item != null ? item : new ItemStack(org.bukkit.Material.AIR));
            }

            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to serialize vault " + e.getMessage());
            return "";
        }
    }

    public void deserializeInto(String data, Inventory inv) {
        if (data == null || data.isEmpty()) return;

        try (ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(data));
             BukkitObjectInputStream bois = new BukkitObjectInputStream(bais)) {

            int size = bois.readInt();
            for (int i = 0; i < Math.min(size, inv.getSize()); i++) {
                ItemStack item = (ItemStack) bois.readObject();
                inv.setItem(i, item);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to deserialize vault: " + e.getMessage());
        }
    }
}