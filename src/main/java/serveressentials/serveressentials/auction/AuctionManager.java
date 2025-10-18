package serveressentials.serveressentials.auction;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import serveressentials.serveressentials.ServerEssentials;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class AuctionManager {

    private final ServerEssentials plugin;
    private final File storageFile;
    private FileConfiguration storageConfig;
    private final List<AuctionItem> auctionItems = new ArrayList<>();

    public AuctionManager(ServerEssentials plugin) {
        this.plugin = plugin;
        this.storageFile = new File(plugin.getDataFolder(), "storage/auctionitems.yml");
        if (!storageFile.exists()) {
            storageFile.getParentFile().mkdirs();
            try { storageFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        storageConfig = YamlConfiguration.loadConfiguration(storageFile);
        loadItems();
    }

    public void addAuctionItem(AuctionItem item) {
        auctionItems.add(item);
        saveItems();
    }

    public void removeAuctionItem(AuctionItem item) {
        auctionItems.remove(item);
        saveItems();
    }

    public List<AuctionItem> getAuctionItems() {
        return new ArrayList<>(auctionItems);
    }

    public List<AuctionItem> getPlayerItems(UUID player) {
        List<AuctionItem> list = new ArrayList<>();
        for (AuctionItem item : auctionItems) if (item.getSeller().equals(player)) list.add(item);
        return list;
    }

    public void saveItems() {
        storageConfig.set("items", null);
        int i = 0;
        for (AuctionItem item : auctionItems) {
            storageConfig.set("items." + i + ".seller", item.getSeller().toString());
            storageConfig.set("items." + i + ".item", item.getItem());
            storageConfig.set("items." + i + ".price", item.getPrice());
            storageConfig.set("items." + i + ".expiration", item.getExpiration());
            i++;
        }
        try { storageConfig.save(storageFile); } catch (IOException e) { e.printStackTrace(); }
    }

    public void loadItems() {
        if (!storageConfig.contains("items")) return;
        auctionItems.clear();
        for (String key : storageConfig.getConfigurationSection("items").getKeys(false)) {
            UUID seller = UUID.fromString(storageConfig.getString("items." + key + ".seller"));
            double price = storageConfig.getDouble("items." + key + ".price");
            long expiration = storageConfig.getLong("items." + key + ".expiration");
            auctionItems.add(new AuctionItem(seller, storageConfig.getItemStack("items." + key + ".item"), price, expiration));
        }
    }
}
