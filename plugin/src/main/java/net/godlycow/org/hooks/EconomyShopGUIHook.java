package net.godlycow.org.hooks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import java.lang.reflect.Method;
import java.util.logging.Level;

public class EconomyShopGUIHook {
    private static EconomyShopGUIHook instance;
    private Plugin economyShopGUI;
    private Method openShopMethod;
    private boolean available = false;

    private EconomyShopGUIHook() {}

    public static EconomyShopGUIHook getInstance() {
        if (instance == null) {
            instance = new EconomyShopGUIHook();
        }
        return instance;
    }

    public boolean init() {
        if (Bukkit.getPluginManager().getPlugin("EconomyShopGUI") == null) {
            this.available = false;
            return false;
        }

        try {
            economyShopGUI = Bukkit.getPluginManager().getPlugin("EconomyShopGUI");
            if (economyShopGUI != null) {
                try {
                    // Try to access EconomyShopGUI's ShopManager
                    Class<?> pluginClass = economyShopGUI.getClass();
                    Method getShopManager = pluginClass.getMethod("getShopManager");
                    Object shopManager = getShopManager.invoke(economyShopGUI);

                    openShopMethod = shopManager.getClass().getMethod("openShop", Player.class);
                    openShopMethod.setAccessible(true);
                } catch (NoSuchMethodException e) {
                    // API method not found, will use command fallback
                    openShopMethod = null;
                }
                this.available = true;
                Bukkit.getLogger().info("[EssC] EconomyShopGUI hook initialized successfully.");
            }
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "[EssC] Failed to initialize EconomyShopGUIHook", e);
            this.available = false;
        }

        return this.available;
    }

    public boolean isAvailable() {
        return available;
    }

    public void openMainGUI(Player player) {
        if (!available || economyShopGUI == null) {
            player.sendMessage("§cShop system is not available.");
            return;
        }

        Bukkit.getScheduler().runTask(economyShopGUI, () -> {
            try {
                if (openShopMethod != null) {
                    Method getShopManager = economyShopGUI.getClass().getMethod("getShopManager");
                    Object shopManager = getShopManager.invoke(economyShopGUI);
                    openShopMethod.invoke(shopManager, player);
                } else {
                    player.performCommand("shop");
                }
            } catch (Exception e) {
                Bukkit.getLogger().warning("[EssC] Failed to open EconomyShopGUI, using command fallback: " + e.getMessage());
                player.performCommand("shop");
            }
        });
    }

    public void cleanup() {
        this.available = false;
        this.economyShopGUI = null;
        this.openShopMethod = null;
        instance = null;
    }
}