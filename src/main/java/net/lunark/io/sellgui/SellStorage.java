package net.lunark.io.sellgui;

import net.lunark.io.database.DatabaseManager;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;


public class SellStorage {
    private final DatabaseManager dbManager;
    private final String poolKey = "sellgui";
    private final Plugin plugin;

    public SellStorage(Plugin plugin, DatabaseManager dbManager) {
        this.plugin = plugin;
        this.dbManager = dbManager;
        initTable();
    }

    private void initTable() {
        String sql = "CREATE TABLE IF NOT EXISTS sell_transactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "player_uuid TEXT NOT NULL, " +
                "player_name TEXT NOT NULL, " +
                "material TEXT NOT NULL, " +
                "quantity INTEGER NOT NULL, " +
                "price_per_item REAL NOT NULL, " +
                "total_price REAL NOT NULL, " +
                "timestamp BIGINT NOT NULL)";

        dbManager.executeUpdate(poolKey, sql).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE,
                    "[SellGUI] Failed to create sell_transactions table: " + ex.getMessage(), ex);
            return null;
        });
    }


    public CompletableFuture<Void> logSale(UUID playerId, String playerName, Material material,
                                           int quantity, double pricePerItem, double totalPrice) {
        String sql = "INSERT INTO sell_transactions (player_uuid, player_name, material, quantity, " +
                "price_per_item, total_price, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?)";

        return dbManager.executeUpdate(poolKey, sql,
                playerId.toString(),
                playerName,
                material.name(),
                quantity,
                pricePerItem,
                totalPrice,
                System.currentTimeMillis()
        ).thenRun(() -> {
            plugin.getLogger().fine(String.format(
                    "Logged sale: %s sold %dx %s for %.2f",
                    playerName, quantity, material.name(), totalPrice));
        });
    }
}