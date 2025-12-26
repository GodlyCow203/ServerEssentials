package net.godlycow.org.commands.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public final class SellConfig {
    private final Plugin plugin;
    private final Map<Material, Double> sellPrices = new HashMap<>();
    public final String guiTitle;
    public final int guiSize;
    public final boolean enabled;
    public final String currencySymbol;

    public SellConfig(Plugin plugin) {
        this.plugin = plugin;

        loadPricesFromShopConfigs();

        FileConfiguration config = plugin.getConfig();
        this.guiTitle = config.getString("sellgui.title", "<gold>💰 Sell Items");
        this.guiSize = Math.max(9, Math.min(54, config.getInt("sellgui.size", 45)));
        this.enabled = config.getBoolean("sellgui.enabled", true);
        this.currencySymbol = config.getString("sellgui.currency-symbol", "$");

        plugin.getLogger().info("[SellGUI] Loaded " + sellPrices.size() + " sellable items from shop configs");

        if (sellPrices.isEmpty()) {
            plugin.getLogger().warning("[SellGUI] No sellable items found! Sell GUI will be non-functional.");
        }
    }


    private void loadPricesFromShopConfigs() {
        File shopFolder = new File(plugin.getDataFolder(), "shop");
        if (!shopFolder.exists() || !shopFolder.isDirectory()) {
            plugin.getLogger().warning("[SellGUI] Shop folder not found at " + shopFolder.getPath());
            plugin.getLogger().warning("[SellGUI] Please ensure shop configs exist with sell-price entries");
            return;
        }

        File[] files = shopFolder.listFiles(f -> f.getName().endsWith(".yml") && !f.getName().equals("main.yml"));
        if (files == null || files.length == 0) {
            plugin.getLogger().warning("[SellGUI] No shop config files found in " + shopFolder.getPath());
            return;
        }

        int loadedCount = 0;
        for (File file : files) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                if (config.contains("items")) {
                    var itemsSection = config.getConfigurationSection("items");
                    if (itemsSection == null) continue;

                    for (String key : itemsSection.getKeys(false)) {
                        String materialPath = "items." + key + ".material";
                        String sellPricePath = "items." + key + ".sell-price";

                        String matName = config.getString(materialPath);
                        double price = config.getDouble(sellPricePath, -1);

                        if (matName != null && price > 0) {
                            Material mat = Material.matchMaterial(matName.toUpperCase());
                            if (mat != null) {
                                sellPrices.put(mat, price);
                                loadedCount++;
                            } else {
                                plugin.getLogger().warning("[SellGUI] Invalid material '" + matName + "' in " + file.getName());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING,
                        "[SellGUI] Error loading " + file.getName() + ": " + e.getMessage(), e);
            }
        }

        plugin.getLogger().info("[SellGUI] Successfully loaded " + loadedCount + " prices from " + files.length + " files");
    }


    public boolean isSellable(Material material) {
        return sellPrices.containsKey(material) && sellPrices.get(material) > 0;
    }


    public double getSellPrice(Material material) {
        return sellPrices.getOrDefault(material, 0.0);
    }


    public int getSellableMaterialsCount() {
        return sellPrices.size();
    }
}