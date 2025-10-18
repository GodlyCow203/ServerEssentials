package serveressentials.serveressentials.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

public class DailyRewardsManager {

    private static JavaPlugin plugin;
    private static FileConfiguration config;
    private static File configFile;

    public DailyRewardsManager(JavaPlugin pl) {
        plugin = pl;
        loadConfig();
    }

    private static void loadConfig() {
        File folder = new File(plugin.getDataFolder(), "config/Daily");
        if (!folder.exists()) folder.mkdirs();

        configFile = new File(folder, "daily.yml");

        // Create from resource if missing
        if (!configFile.exists()) {
            try (InputStream in = plugin.getResource("config/Daily/daily.yml")) {
                if (in != null) {
                    Files.copy(in, configFile.toPath());
                } else {
                    plugin.getLogger().warning("[ServerEssentials] Missing resource: config/Daily/daily.yml in jar!");
                    configFile.createNewFile();
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to copy daily.yml from resources!");
                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    // ✅ Static reload method for /sereload
    public static void reload() {
        if (plugin == null) {
            throw new IllegalStateException("DailyRewardsManager plugin reference is null!");
        }

        if (configFile == null || !configFile.exists()) {
            loadConfig(); // recreate if missing
        } else {
            config = YamlConfiguration.loadConfiguration(configFile);
        }

        plugin.getLogger().info("DailyRewardsManager reloaded.");
    }

    public FileConfiguration getConfig() {
        return config;
    }

    // -----------------------------
    // Reward Access Methods
    // -----------------------------

    /**
     * Returns a list of all reward day keys (e.g., ["1", "2", "3"]).
     */
    public List<String> getRewardDays() {
        if (config.getConfigurationSection("rewards") == null) return List.of();
        return new ArrayList<>(config.getConfigurationSection("rewards").getKeys(false));
    }

    /**
     * Returns the list of items for a given day.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getRewardItems(String day) {
        List<Map<?, ?>> raw = config.getMapList("rewards." + day + ".items");
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<?, ?> map : raw) {
            list.add((Map<String, Object>) map);
        }
        return list;
    }

    /**
     * Returns the GUI page number for a given day (default 1).
     */
    public int getRewardPage(String day) {
        return config.getInt("rewards." + day + ".page", 1);
    }

    /**
     * Returns the inventory slot for a given day (default 0).
     */
    public int getRewardSlot(String day) {
        return config.getInt("rewards." + day + ".slot", 0);
    }

    /**
     * Returns the first reward item for a day.
     */
    public Map<String, Object> getFirstRewardItem(String day) {
        List<Map<String, Object>> items = getRewardItems(day);
        return items.isEmpty() ? Map.of() : items.get(0);
    }

    /**
     * Returns the total number of GUI pages.
     */
    public int getTotalPages() {
        int pages = 1;
        for (String day : getRewardDays()) {
            pages = Math.max(pages, getRewardPage(day));
        }
        return pages;
    }
}
