package serveressentials.serveressentials;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AuctionManager {
    private final List<AuctionItem> items = new ArrayList<>();

    public void addItem(AuctionItem item) {
        items.add(item);
    }

    public List<AuctionItem> getItems() {
        return new ArrayList<>(items);
    }

    public AuctionItem getItemById(int id) {
        return items.stream().filter(item -> item.getId() == id).findFirst().orElse(null);
    }

    public void removeItemsBySeller(UUID seller) {
        items.removeIf(item -> item.getSeller().equals(seller));
    }

    public void removeItem(AuctionItem item) {
        items.remove(item);
    }


    public List<AuctionItem> getItemsByPage(int page, int pageSize) {
        int start = page * pageSize;
        int end = Math.min(start + pageSize, items.size());
        if (start >= items.size()) return new ArrayList<>();
        return items.subList(start, end);
    }

    public int getTotalPages(int pageSize) {
        return (int) Math.ceil((double) items.size() / pageSize);
    }
}
