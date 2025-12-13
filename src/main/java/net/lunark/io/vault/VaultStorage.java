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
    private final String poolKey = "vaults";
    private final Plugin plugin;

    public VaultStorage(Plugin plugin, DatabaseManager dbManager) {
        this.plugin = plugin;
        this.dbManager = dbManager;
        initTable();
    }

    private void initTable() {
        String sql = "CREATE TABLE IF NOT EXISTS vaults (" +
                "uuid TEXT NOT NULL, " +
                "vault_number INTEGER NOT NULL, " +
                "inventory_data TEXT NOT NULL, " +
                "PRIMARY KEY (uuid, vault_number))";
        dbManager.executeUpdate(poolKey, sql);
    }

    public CompletableFuture<Void> saveVault(UUID uuid, int number, Inventory inventory) {
        String serialized = serializeInventory(inventory);
        String sql = "INSERT OR REPLACE INTO vaults VALUES (?, ?, ?)";
        return dbManager.executeUpdate(poolKey, sql, uuid.toString(), number, serialized);
    }

    public CompletableFuture<Void> clearVault(UUID uuid, int number) {
        String sql = "DELETE FROM vaults WHERE uuid = ? AND vault_number = ?";
        return dbManager.executeUpdate(poolKey, sql, uuid.toString(), number);
    }

    public CompletableFuture<Optional<String>> loadVaultData(UUID uuid, int number) {
        String sql = "SELECT inventory_data FROM vaults WHERE uuid = ? AND vault_number = ?";
        return dbManager.executeQuery(poolKey, sql,
                rs -> rs.next() ? rs.getString("inventory_data") : null,
                uuid.toString(), number);
    }

    public CompletableFuture<Boolean> hasVault(UUID uuid, int number) {
        String sql = "SELECT 1 FROM vaults WHERE uuid = ? AND vault_number = ?";
        return dbManager.executeQuery(poolKey, sql,
                        rs -> rs.next(),
                        uuid.toString(), number)
                .thenApply(opt -> opt.orElse(false));
    }

    private String serializeInventory(Inventory inv) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             BukkitObjectOutputStream boos = new BukkitObjectOutputStream(baos)) {
            boos.writeInt(inv.getSize());
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack item = inv.getItem(i);
                boos.writeObject(item);
            }
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to serialize vault: " + e.getMessage());
            return "";
        }
    }

    public void deserializeInventory(Inventory inv, String data) {
        if (data == null || data.isEmpty()) return;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(data));
             BukkitObjectInputStream bois = new BukkitObjectInputStream(bais)) {
            int size = bois.readInt();
            for (int i = 0; i < size && i < inv.getSize(); i++) {
                ItemStack item = (ItemStack) bois.readObject();
                inv.setItem(i, item);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to deserialize vault: " + e.getMessage());
        }
    }
}