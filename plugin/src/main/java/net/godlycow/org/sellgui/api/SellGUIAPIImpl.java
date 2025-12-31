package net.godlycow.org.sellgui.api;

import com.serveressentials.api.sellgui.SellGUIAPI;
import com.serveressentials.api.sellgui.SellGUILayout;
import com.serveressentials.api.sellgui.SellTransaction;
import com.serveressentials.api.sellgui.SellableItem;
import com.serveressentials.api.sellgui.event.SellGUIItemSellEvent;
import com.serveressentials.api.sellgui.event.SellGUIOpenEvent;
import com.serveressentials.api.sellgui.event.SellGUITransactionCompleteEvent;
import net.godlycow.org.ServerEssentials;
import net.godlycow.org.commands.config.SellConfig;
import net.godlycow.org.economy.eco.EconomyManager;
import net.godlycow.org.economy.eco.EconomyResponse;
import net.godlycow.org.sellgui.gui.SellGUIManager;
import net.godlycow.org.sellgui.storage.SellStorage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class SellGUIAPIImpl implements SellGUIAPI {
    private final @NotNull ServerEssentials plugin;
    private final @NotNull SellConfig config;
    private final @NotNull SellStorage storage;
    private final @NotNull SellGUIManager guiManager;
    private final @NotNull EconomyManager economyManager;

    public SellGUIAPIImpl(@NotNull ServerEssentials plugin, @NotNull SellConfig config,
                          @NotNull SellStorage storage, @NotNull SellGUIManager guiManager,
                          @NotNull EconomyManager economyManager) {
        this.plugin = plugin;
        this.config = config;
        this.storage = storage;
        this.guiManager = guiManager;
        this.economyManager = economyManager;
    }

    @Override
    public @NotNull CompletableFuture<Boolean> openSellGUI(@NotNull Player player) {
        return CompletableFuture.supplyAsync(() -> {
            if (!config.enabled) {
                return false;
            }

            if (!economyManager.isEnabled()) {
                return false;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                guiManager.openSellGUI(player);
                Bukkit.getPluginManager().callEvent(new SellGUIOpenEvent(player));
            });

            return true;
        });
    }

    @Override
    public boolean isSellable(@NotNull Material material) {
        return config.isSellable(material);
    }

    @Override
    public double getSellPrice(@NotNull Material material) {
        return config.getSellPrice(material);
    }

    @Override
    public @NotNull CompletableFuture<List<SellableItem>> getSellableItems() {
        return CompletableFuture.supplyAsync(() ->
                config.getSellPrices().entrySet().stream()
                        .map(entry -> new SellableItem(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList())
        );
    }

    @Override
    public @NotNull SellGUILayout getLayout() {
        return new SellGUILayout(config.guiTitle, config.guiSize, config.currencySymbol);
    }

    @Override
    public @NotNull CompletableFuture<SellTransaction> processSellTransaction(@NotNull Player player,
                                                                              @NotNull Map<Material, Integer> items) {
        return CompletableFuture.supplyAsync(() -> {
            double totalValue = 0.0;
            int totalQuantity = 0;
            Material primaryMaterial = null;

            for (Map.Entry<Material, Integer> entry : items.entrySet()) {
                Material material = entry.getKey();
                int quantity = entry.getValue();

                if (!isSellable(material)) {
                    continue;
                }

                double price = getSellPrice(material);
                double itemValue = price * quantity;
                totalValue += itemValue;
                totalQuantity += quantity;

                if (primaryMaterial == null) {
                    primaryMaterial = material;
                }

                Bukkit.getScheduler().runTask(plugin, () -> {
                    Bukkit.getPluginManager().callEvent(new SellGUIItemSellEvent(player, material, quantity, price, itemValue));
                });
            }

            if (totalValue <= 0) {
                throw new IllegalArgumentException("No sellable items provided");
            }

            EconomyResponse response = economyManager.deposit(player, totalValue);

            if (!response.success()) {
                throw new RuntimeException("Economy transaction failed: ");
            }

            if (primaryMaterial == null) {
                primaryMaterial = Material.AIR;
            }

            SellTransaction transaction = new SellTransaction(
                    player.getUniqueId(),
                    player.getName(),
                    primaryMaterial,
                    totalQuantity,
                    totalValue / totalQuantity,
                    totalValue,
                    System.currentTimeMillis()
            );

            logTransaction(transaction).join();

            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.getPluginManager().callEvent(new SellGUITransactionCompleteEvent(player, transaction));
            });

            return transaction;
        });
    }

    @Override
    public @NotNull CompletableFuture<Void> logTransaction(@NotNull SellTransaction transaction) {
        return storage.logSale(
                transaction.getPlayerId(),
                transaction.getPlayerName(),
                transaction.getMaterial(),
                transaction.getQuantity(),
                transaction.getPricePerItem(),
                transaction.getTotalPrice()
        );
    }

    @Override
    public boolean isEnabled() {
        return config.enabled;
    }

    @Override
    public @NotNull String getCurrencySymbol() {
        return config.currencySymbol;
    }

    @Override
    public @NotNull CompletableFuture<Void> reload() {
        return CompletableFuture.runAsync(() -> {
            plugin.reloadConfig();
            // coming soon
            plugin.getLogger().info("[ServerEssentials] SellGUI configuration reloaded");
        });
    }
}