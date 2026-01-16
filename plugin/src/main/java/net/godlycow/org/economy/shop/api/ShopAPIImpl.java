package net.godlycow.org.economy.shop.api;

import com.serveressentials.api.shop.ShopAPI;
import com.serveressentials.api.shop.ShopSection;
import com.serveressentials.api.shop.ShopItem;
import com.serveressentials.api.shop.ShopLayout;
import com.serveressentials.api.shop.ShopButton;
import net.godlycow.org.commands.config.ShopConfig;
import net.godlycow.org.economy.shop.ShopDataManager;
import net.godlycow.org.economy.shop.config.ShopSectionConfig;
import net.godlycow.org.economy.shop.gui.ShopGUIManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.HashMap;
import java.util.stream.Collectors;

public class ShopAPIImpl implements ShopAPI {
    private final ShopConfig config;
    private final ShopGUIManager guiManager;
    private final ShopDataManager dataManager;

    public ShopAPIImpl(ShopConfig config, ShopGUIManager guiManager, ShopDataManager dataManager) {
        this.config = config;
        this.guiManager = guiManager;
        this.dataManager = dataManager;
    }

    @Override
    public void openShop(Player player) {
        Bukkit.getScheduler().runTask(guiManager.getPlugin(), () -> guiManager.openMainGUI(player));
    }

    @Override
    public void openShopSection(Player player, String sectionName) {
        Bukkit.getScheduler().runTask(guiManager.getPlugin(), () -> guiManager.openSectionGUI(player, sectionName, 1));
    }

    @Override
    public CompletableFuture<Boolean> reloadShop() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Bukkit.getScheduler().callSyncMethod(guiManager.getPlugin(), () -> {
                    guiManager.reloadConfigs(true);
                    guiManager.refreshOpenInventories();
                    return null;
                }).get();
                return true;
            } catch (Exception e) {
                return false;
            }
        });
    }

    @Override
    public boolean isShopEnabled() {
        return config.enabled;
    }

    @Override
    public Collection<String> getSectionNames() {
        return guiManager.getSectionCache().keySet();
    }

    @Override
    public ShopSection getSection(String sectionName) {
        ShopSectionConfig config = guiManager.getSectionConfig(sectionName);
        if (config == null) return null;
        return convertSection(config);
    }

    private ShopSection convertSection(net.godlycow.org.economy.shop.config.ShopSectionConfig config) {
        Map<Integer, ShopLayout> layout = config.layout.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> new ShopLayout(e.getValue().material, e.getValue().name, e.getValue().lore, e.getValue().clickable)
                ));

        Map<String, ShopItem> items = config.items.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> new ShopItem(
                                e.getValue().material,
                                e.getValue().amount,
                                e.getValue().name,
                                e.getValue().lore,
                                e.getValue().buyPrice,
                                e.getValue().sellPrice,
                                e.getValue().customItemId,
                                e.getValue().slot,
                                e.getValue().page,
                                e.getValue().clickable
                        )
                ));

        return new ShopSection(
                config.title,
                config.size,
                config.pages,
                config.playerHeadSlot,
                config.closeButtonSlot,
                layout,
                items
        );
    }
}