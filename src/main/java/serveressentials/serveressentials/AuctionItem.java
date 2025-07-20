package serveressentials.serveressentials;

import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class AuctionItem {
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    private final int id;
    private final UUID seller;
    private final ItemStack item;
    private final double price;

    public AuctionItem(UUID seller, ItemStack item, double price, int id) {
        this.id = id;
        this.seller = seller;
        this.item = item;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public UUID getSeller() {
        return seller;
    }

    public ItemStack getItem() {
        return item;
    }

    public double getPrice() {
        return price;
    }

    public static int getNextId() {
        return COUNTER.getAndIncrement();
    }

    // ✅ Add this
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuctionItem)) return false;
        AuctionItem that = (AuctionItem) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
