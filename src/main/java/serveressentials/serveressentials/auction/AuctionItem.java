package serveressentials.serveressentials.auction;


import org.bukkit.inventory.ItemStack;
import java.io.Serializable;
import java.util.UUID;

public class AuctionItem implements Serializable {
    private final UUID seller;
    private final ItemStack item;
    private final double price;
    private final long expiration;

    public AuctionItem(UUID seller, ItemStack item, double price, long expiration) {
        this.seller = seller;
        this.item = item;
        this.price = price;
        this.expiration = expiration;
    }

    public UUID getSeller() { return seller; }
    public ItemStack getItem() { return item; }
    public double getPrice() { return price; }
    public long getExpiration() { return expiration; }
}
