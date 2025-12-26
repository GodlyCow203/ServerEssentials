package net.godlycow.org.auction.storage;

import net.godlycow.org.auction.model.AuctionItem;
import net.godlycow.org.database.DatabaseManager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AuctionStorage {
    private final DatabaseManager dbManager;
    private final String poolKey = "auction";

    public AuctionStorage(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        initTable();
    }

    private void initTable() {
        String sql = "CREATE TABLE IF NOT EXISTS auction_items (" +
                "id TEXT PRIMARY KEY, " +
                "seller TEXT NOT NULL, " +
                "item_base64 TEXT NOT NULL, " +
                "price REAL NOT NULL, " +
                "expiration BIGINT NOT NULL, " +
                "created_at BIGINT NOT NULL)";
        dbManager.executeUpdate(poolKey, sql);
    }

    public CompletableFuture<Void> addItem(AuctionItem item) {
        String sql = "INSERT OR REPLACE INTO auction_items VALUES (?, ?, ?, ?, ?, ?)";
        String itemBase64 = itemStackToBase64(item.getItem());
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
        String sql = "DELETE FROM auction_items WHERE id = ?";
        return dbManager.executeUpdate(poolKey, sql, itemId.toString());
    }

    public CompletableFuture<List<AuctionItem>> getAllActiveItems() {
        String sql = "SELECT * FROM auction_items WHERE expiration > ?";
        return dbManager.executeQuery(poolKey, sql,
                rs -> {
                    List<AuctionItem> items = new ArrayList<>();
                    while (rs.next()) {
                        items.add(mapAuctionItem(rs));
                    }
                    return items;
                },
                System.currentTimeMillis()
        ).thenApply(opt -> opt.orElse(new ArrayList<>()));
    }

    public CompletableFuture<List<AuctionItem>> getPlayerItems(UUID playerId) {
        String sql = "SELECT * FROM auction_items WHERE seller = ? AND expiration > ?";
        return dbManager.executeQuery(poolKey, sql,
                rs -> {
                    List<AuctionItem> items = new ArrayList<>();
                    while (rs.next()) {
                        items.add(mapAuctionItem(rs));
                    }
                    return items;
                },
                playerId.toString(),
                System.currentTimeMillis()
        ).thenApply(opt -> opt.orElse(new ArrayList<>()));
    }

    public CompletableFuture<Boolean> itemExists(UUID itemId) {
        String sql = "SELECT 1 FROM auction_items WHERE id = ?";
        return dbManager.executeQuery(poolKey, sql,
                rs -> rs.next(),
                itemId.toString()
        ).thenApply(opt -> opt.orElse(false));
    }

    private AuctionItem mapAuctionItem(ResultSet rs) throws SQLException {
        UUID id = UUID.fromString(rs.getString("id"));
        UUID seller = UUID.fromString(rs.getString("seller"));
        ItemStack item = base64ToItemStack(rs.getString("item_base64"));
        double price = rs.getDouble("price");
        long expiration = rs.getLong("expiration");
        return new AuctionItem(id, seller, item, price, expiration);
    }

    private String itemStackToBase64(ItemStack item) {
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
        String sql = "SELECT * FROM auction_items WHERE id = ?";
        return dbManager.executeQuery(poolKey, sql,
                this::mapAuctionItem,
                itemId.toString()
        );
    }

    private ItemStack base64ToItemStack(String base64) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(base64));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();
            return item;
        } catch (Exception e) {
            throw new RuntimeException("Unable to load item stack", e);
        }
    }
}