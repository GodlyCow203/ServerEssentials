package net.godlycow.org.auction.api;

import com.serveressentials.api.auction.AuctionAPI;
import com.serveressentials.api.auction.AuctionItem;
import com.serveressentials.api.auction.event.AuctionListEvent;
import com.serveressentials.api.auction.event.AuctionPurchaseEvent;
import com.serveressentials.api.auction.event.AuctionRemoveEvent;
import net.godlycow.org.auction.gui.AuctionGUIListener;
import net.godlycow.org.auction.storage.AuctionStorage;
import net.godlycow.org.commands.config.AuctionConfig;
import net.godlycow.org.economy.eco.EconomyManager;
import net.godlycow.org.economy.eco.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class AuctionAPIImpl implements AuctionAPI {
    private final AuctionConfig config;
    private final AuctionGUIListener guiListener;
    private final AuctionStorage storage;
    private final EconomyManager economyManager;
    private final net.godlycow.org.ServerEssentials plugin;

    public AuctionAPIImpl(net.godlycow.org.ServerEssentials plugin, AuctionConfig config,
                          AuctionGUIListener guiListener, AuctionStorage storage,
                          EconomyManager economyManager) {
        this.plugin = plugin;
        this.config = config;
        this.guiListener = guiListener;
        this.storage = storage;
        this.economyManager = economyManager;
    }

    @Override
    public void openAuction(Player player) {
        if (!config.enabled) return;
        Bukkit.getScheduler().runTask(plugin, () -> guiListener.openAuctionGUI(player, 1));
    }

    @Override
    public void openMyAuctionItems(Player player) {
        if (!config.enabled) return;
        Bukkit.getScheduler().runTask(plugin, () -> guiListener.openPlayerItemsGUI(player, 1));
    }

    @Override
    public CompletableFuture<Boolean> addAuctionItem(Player seller, ItemStack item, double price) {
        if (!config.enabled || !economyManager.isEnabled()) {
            return CompletableFuture.completedFuture(false);
        }

        if (price <= 0 || price > getMaxPriceLimit()) {
            return CompletableFuture.completedFuture(false);
        }

        if (item.getAmount() > config.maxSellLimit) {
            return CompletableFuture.completedFuture(false);
        }

        long expirationTime = System.currentTimeMillis() + (config.expirationDays * 24L * 60L * 60L * 1000L);
        net.godlycow.org.auction.model.AuctionItem internalItem =
                new net.godlycow.org.auction.model.AuctionItem(seller.getUniqueId(), item, price, expirationTime);

        return storage.addItem(internalItem)
                .thenApply(v -> {
                    AuctionListEvent event = new AuctionListEvent(seller, item, price);
                    Bukkit.getPluginManager().callEvent(event);
                    return true;
                })
                .exceptionally(ex -> {
                    plugin.getLogger().severe("Failed to add auction item: " + ex.getMessage());
                    return false;
                });
    }

    @Override
    public CompletableFuture<Boolean> removeAuctionItem(UUID itemId) {
        if (!config.enabled) return CompletableFuture.completedFuture(false);

        return storage.getItemData(itemId)
                .thenCompose(optItem -> {
                    if (optItem.isEmpty()) {
                        return CompletableFuture.completedFuture(false);
                    }

                    com.serveressentials.api.auction.AuctionItem apiItem = convertToDTO(optItem.get());
                    return storage.removeItem(itemId)
                            .thenApply(v -> {
                                Player seller = Bukkit.getPlayer(apiItem.getSeller());
                                if (seller != null && seller.isOnline()) {
                                    AuctionRemoveEvent event = new AuctionRemoveEvent(seller, apiItem.getItem(), itemId);
                                    Bukkit.getPluginManager().callEvent(event);
                                }
                                return true;
                            });
                })
                .exceptionally(ex -> {
                    plugin.getLogger().severe("Failed to remove auction item: " + ex.getMessage());
                    return false;
                });
    }

    @Override
    public CompletableFuture<Collection<AuctionItem>> getActiveItems() {
        return storage.getAllActiveItems()
                .thenApply(items -> items.stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList()));
    }

    @Override
    public CompletableFuture<Collection<AuctionItem>> getPlayerItems(UUID playerId) {
        return storage.getPlayerItems(playerId)
                .thenApply(items -> items.stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList()));
    }

    @Override
    public boolean isAuctionEnabled() {
        return config.enabled;
    }

    @Override
    public double getMaxPriceLimit() {
        return config.maxPriceLimit;
    }

    @Override
    public int getMaxItemsPerPlayer() {
        return config.maxItemsPerPlayer;
    }

    private AuctionItem convertToDTO(net.godlycow.org.auction.model.AuctionItem internal) {
        return new AuctionItem(
                internal.getId(),
                internal.getSeller(),
                internal.getItem(),
                internal.getPrice(),
                internal.getExpiration(),
                0
        );
    }
}