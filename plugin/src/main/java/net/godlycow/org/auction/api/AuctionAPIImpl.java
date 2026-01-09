package net.godlycow.org.auction.api;

import com.serveressentials.api.auction.AuctionAPI;
import com.serveressentials.api.auction.AuctionItem;
import com.serveressentials.api.auction.event.AuctionListEvent;
import com.serveressentials.api.auction.event.AuctionRemoveEvent;
import net.godlycow.org.auction.gui.AuctionGUIListener;
import net.godlycow.org.auction.storage.AuctionStorage;
import net.godlycow.org.commands.config.AuctionConfig;
import net.godlycow.org.economy.eco.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.logging.Level;

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
        if (!config.enabled || !economyManager.isEnabled()) {
            plugin.getLogger().log(Level.WARNING, "Auction system disabled when opening GUI for " + player.getName());
            return;
        }
        Bukkit.getScheduler().runTask(plugin, () -> guiListener.openAuctionGUI(player, 1));
    }

    @Override
    public void openMyAuctionItems(Player player) {
        if (!config.enabled || !economyManager.isEnabled()) {
            plugin.getLogger().log(Level.WARNING, "Auction system disabled when opening player items for " + player.getName());
            return;
        }
        Bukkit.getScheduler().runTask(plugin, () -> guiListener.openPlayerItemsGUI(player, 1));
    }

    @Override
    public CompletableFuture<Boolean> addAuctionItem(Player seller, ItemStack item, double price) {
        if (!config.enabled || !economyManager.isEnabled()) {
            plugin.getLogger().log(Level.WARNING, "Auction system disabled when adding item");
            return CompletableFuture.completedFuture(false);
        }

        if (price <= 0 || price > getMaxPriceLimit()) {
            plugin.getLogger().log(Level.WARNING, "Invalid auction price: " + price + " from " + seller.getName());
            return CompletableFuture.completedFuture(false);
        }

        if (item.getAmount() > config.maxSellLimit) {
            plugin.getLogger().log(Level.WARNING, "Auction item exceeds max limit: " + item.getAmount());
            return CompletableFuture.completedFuture(false);
        }

        long expirationTime = System.currentTimeMillis() + (config.expirationDays * 24L * 60L * 60L * 1000L);
        net.godlycow.org.auction.model.AuctionItem internalItem =
                new net.godlycow.org.auction.model.AuctionItem(seller.getUniqueId(), item, price, expirationTime);

        return storage.addItem(internalItem)
                .thenApply(v -> {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        AuctionListEvent event = new AuctionListEvent(seller, item, price);
                        Bukkit.getPluginManager().callEvent(event);
                    });
                    return true;
                })
                .exceptionally(ex -> {
                    plugin.getLogger().log(Level.SEVERE, "Failed to add auction item", ex);
                    return false;
                });
    }

    @Override
    public CompletableFuture<Boolean> removeAuctionItem(UUID itemId) {
        if (!config.enabled) {
            plugin.getLogger().log(Level.WARNING, "Auction system disabled when removing item");
            return CompletableFuture.completedFuture(false);
        }

        return storage.getItemData(itemId)
                .thenCompose(optItem -> {
                    if (optItem.isEmpty()) {
                        plugin.getLogger().log(Level.WARNING, "Auction item not found for removal: " + itemId);
                        return CompletableFuture.completedFuture(false);
                    }

                    com.serveressentials.api.auction.AuctionItem apiItem = convertToDTO(optItem.get());
                    return storage.removeItem(itemId)
                            .thenApply(v -> {
                                Bukkit.getScheduler().runTask(plugin, () -> {
                                    Player seller = Bukkit.getPlayer(apiItem.getSeller());
                                    if (seller != null && seller.isOnline()) {
                                        AuctionRemoveEvent event = new AuctionRemoveEvent(seller, apiItem.getItem(), itemId);
                                        Bukkit.getPluginManager().callEvent(event);
                                    }
                                });
                                return true;
                            });
                })
                .exceptionally(ex -> {
                    plugin.getLogger().log(Level.SEVERE, "Failed to remove auction item", ex);
                    return false;
                });
    }

    @Override
    public CompletableFuture<@NotNull List<AuctionItem>> getActiveItems() {
        return storage.getAllActiveItems()
                .thenApply(items -> items.stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList()))
                .exceptionally(ex -> {
                    plugin.getLogger().log(Level.SEVERE, "Failed to get active auction items", ex);
                    return List.of();
                });
    }

    @Override
    public CompletableFuture<@NotNull List<AuctionItem>> getPlayerItems(UUID playerId) {
        return storage.getPlayerItems(playerId)
                .thenApply(items -> items.stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList()))
                .exceptionally(ex -> {
                    plugin.getLogger().log(Level.SEVERE, "Failed to get player auction items", ex);
                    return List.of();
                });
    }

    @Override
    public boolean isAuctionEnabled() {
        return config.enabled && economyManager.isEnabled();
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