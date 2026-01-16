package net.godlycow.org.auction.storage;

import net.godlycow.org.auction.model.AuctionItem;
import net.godlycow.org.database.DatabaseManager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class AuctionStorage {
    private final DatabaseManager dbManager;
    private final String poolKey = "auction";

    public AuctionStorage(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        initTable();
    }

    private void initTable() {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS auction_items (" +
                    "id TEXT PRIMARY KEY, " +
                    "seller TEXT NOT NULL, " +
                    "item_base64 TEXT NOT NULL, " +
                    "price REAL NOT NULL, " +
                    "expiration BIGINT NOT NULL, " +
                    "created_at BIGINT NOT NULL)";
            dbManager.executeUpdate(poolKey, sql);
        } catch (Exception e) {
            // Log but don't crash  :))
            System.err.println("Failed to initialize auction table: " + e.getMessage());
        }
    }

    public CompletableFuture<Void> addItem(AuctionItem item) {
        if (item == null || item.getItem() == null || item.getItem().getType().isAir()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid auction item"));
        }

        String sql = "INSERT OR REPLACE INTO auction_items VALUES (?, ?, ?, ?, ?, ?)";
        String itemBase64;
        try {
            itemBase64 = itemStackToBase64(item.getItem());
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }

        return dbManager.executeUpdate(poolKey, sql,
                item.getId().toString(),
                item.getSeller().toString(),
                itemBase64,
                item.getPrice(),
                item.getExpiration(),
                System.currentTimeMillis()
        );
    }

    public CompletableFuture<Void> removeItem(UUID itemId) {
        if (itemId == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Item ID cannot be null"));
        }

        String sql = "DELETE FROM auction_items WHERE id = ?";
        return dbManager.executeUpdate(poolKey, sql, itemId.toString());
    }

    public CompletableFuture<List<AuctionItem>> getAllActiveItems() {
        String sql = "SELECT * FROM auction_items WHERE expiration > ?";
        return dbManager.executeQuery(poolKey, sql,
                rs -> {
                    List<AuctionItem> items = new ArrayList<>();
                    while (rs.next()) {
                        AuctionItem item = mapAuctionItem(rs);
                        if (item != null) {
                            items.add(item);
                        }
                    }
                    return items;
                },
                System.currentTimeMillis()
        ).thenApply(opt -> opt.orElse(new ArrayList<>()));
    }

    public CompletableFuture<List<AuctionItem>> getPlayerItems(UUID playerId) {
        if (playerId == null) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        String sql = "SELECT * FROM auction_items WHERE seller = ? AND expiration > ?";
        return dbManager.executeQuery(poolKey, sql,
                rs -> {
                    List<AuctionItem> items = new ArrayList<>();
                    while (rs.next()) {
                        AuctionItem item = mapAuctionItem(rs);
                        if (item != null) {
                            items.add(item);
                        }
                    }
                    return items;
                },
                playerId.toString(),
                System.currentTimeMillis()
        ).thenApply(opt -> opt.orElse(new ArrayList<>()));
    }

    public CompletableFuture<Boolean> itemExists(UUID itemId) {
        if (itemId == null) {
            return CompletableFuture.completedFuture(false);
        }

        String sql = "SELECT 1 FROM auction_items WHERE id = ?";
        return dbManager.executeQuery(poolKey, sql,
                rs -> rs.next(),
                itemId.toString()
        ).thenApply(opt -> opt.orElse(false));
    }

    private AuctionItem mapAuctionItem(ResultSet rs) throws SQLException {
        try {
            UUID id = UUID.fromString(rs.getString("id"));
            UUID seller = UUID.fromString(rs.getString("seller"));
            ItemStack item = base64ToItemStack(rs.getString("item_base64"));
            double price = rs.getDouble("price");
            long expiration = rs.getLong("expiration");

            if (item == null || item.getType().isAir()) {
                return null;
            }

            return new AuctionItem(id, seller, item, price, expiration);
        } catch (Exception e) {
            System.err.println("Failed to map auction item from database: " + e.getMessage());
            return null;
        }
    }

    private String itemStackToBase64(ItemStack item) {
        if (item == null) {
            throw new IllegalArgumentException("ItemStack cannot be null");
        }

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(item);
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Unable to save item stack", e);
        }
    }

    public CompletableFuture<Optional<AuctionItem>> getItemData(UUID itemId) {
        if (itemId == null) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        String sql = "SELECT * FROM auction_items WHERE id = ?";
        return dbManager.executeQuery(poolKey, sql,
                this::mapAuctionItem,
                itemId.toString()
        );
    }

    private ItemStack base64ToItemStack(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return null;
        }

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(base64));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();
            return item;
        } catch (Exception e) {
            System.err.println("Unable to load item stack from base64: " + e.getMessage());
            return null;
        }
    }
}