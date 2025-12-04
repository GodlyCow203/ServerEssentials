package net.lunark.io.daily;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DailyConfig {
    private final Plugin plugin;
    private final File configFile;
    private FileConfiguration config;

    public final int cooldownHours;
    public final String guiTitle;
    public final int guiRows;
    public final Map<Integer, DailyReward> rewards;

    public DailyConfig(Plugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "daily.yml");
        saveDefaultConfig();
        this.config = YamlConfiguration.loadConfiguration(configFile);

        this.cooldownHours = config.getInt("cooldown", 24);
        this.guiTitle = config.getString("gui.title", "Daily Rewards");
        this.guiRows = Math.max(1, Math.min(6, config.getInt("gui.rows", 6)));
        this.rewards = loadRewards();
    }

    private void saveDefaultConfig() {
        if (!configFile.exists()) {
            try (InputStream in = plugin.getResource("daily.yml")) {
                if (in != null) {
                    java.nio.file.Files.copy(in, configFile.toPath());
                } else {
                    configFile.createNewFile();
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to save default daily.yml!");
            }
        }
    }

    private Map<Integer, DailyReward> loadRewards() {
        Map<Integer, DailyReward> loaded = new HashMap<>();
        ConfigurationSection rewardsSection = config.getConfigurationSection("rewards");

        if (rewardsSection == null) {
            plugin.getLogger().warning("No rewards section found in daily.yml!");
            return loaded;
        }

        for (String key : rewardsSection.getKeys(false)) {
            try {
                int day = Integer.parseInt(key);
                ConfigurationSection rewardSection = rewardsSection.getConfigurationSection(key);

                if (rewardSection == null) continue;

                int slot = rewardSection.getInt("slot", -1);
                int page = rewardSection.getInt("page", 1);

                List<DailyReward.RewardItem> items = new ArrayList<>();
                List<?> itemList = rewardSection.getList("items");

                if (itemList != null) {
                    for (Object obj : itemList) {
                        if (obj instanceof ConfigurationSection itemSection) {
                            items.add(parseRewardItem(itemSection));
                        } else if (obj instanceof Map<?, ?> itemMap) {
                            items.add(parseRewardItemFromMap(itemMap));
                        }
                    }
                }

                DailyReward reward = new DailyReward(day, slot, page, items);
                loaded.put(day, reward);

            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Invalid day key: " + key + " in daily.yml");
            }
        }

        return loaded;
    }

    private DailyReward.RewardItem parseRewardItem(ConfigurationSection section) {
        String materialName = section.getString("type", "DIAMOND");
        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            material = Material.DIAMOND;
            plugin.getLogger().warning("Invalid material: " + materialName);
        }

        int amount = section.getInt("amount", 1);
        String name = section.getString("name", "");

        List<String> lore = new ArrayList<>();
        if (section.contains("lore")) {
            lore = section.getStringList("lore");
        }

        boolean glow = section.getBoolean("glow", false);

        Map<String, Integer> enchantments = new HashMap<>();
        if (section.contains("enchantments")) {
            List<?> enchList = section.getList("enchantments");
            if (enchList != null) {
                for (Object obj : enchList) {
                    if (obj instanceof String enchStr) {
                        String[] parts = enchStr.split(":");
                        if (parts.length == 2) {
                            try {
                                enchantments.put(parts[0], Integer.parseInt(parts[1]));
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }
            }
        }

        Map<String, String> nbt = new HashMap<>();
        if (section.contains("nbt")) {
            ConfigurationSection nbtSection = section.getConfigurationSection("nbt");
            if (nbtSection != null) {
                for (String key : nbtSection.getKeys(false)) {
                    nbt.put(key, nbtSection.getString(key, ""));
                }
            }
        }

        return new DailyReward.RewardItem(material, amount, name, lore, enchantments, glow, nbt);
    }

    private DailyReward.RewardItem parseRewardItemFromMap(Map<?, ?> map) {
        // Use get() instead of getOrDefault() to avoid type issues with Map<?, ?>
        Object typeObj = map.get("type");
        String materialName = typeObj instanceof String ? (String) typeObj : "DIAMOND";

        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            material = Material.DIAMOND;
            plugin.getLogger().warning("Invalid material: " + materialName);
        }

        Object amtObj = map.get("amount");
        int amount = 1;
        if (amtObj instanceof Number) {
            amount = ((Number) amtObj).intValue();
        } else if (amtObj != null) {
            try {
                amount = Integer.parseInt(amtObj.toString());
            } catch (NumberFormatException ignored) {}
        }

        Object nameObj = map.get("name");
        String name = nameObj instanceof String ? (String) nameObj : "";

        List<String> lore = new ArrayList<>();
        Object loreObj = map.get("lore");
        if (loreObj instanceof List) {
            for (Object line : (List<?>) loreObj) {
                if (line instanceof String) {
                    lore.add((String) line);
                }
            }
        }

        Object glowObj = map.get("glow");
        boolean glow = glowObj instanceof Boolean ? (Boolean) glowObj : false;

        Map<String, Integer> enchantments = new HashMap<>();
        // Note: Enchantments not parsed from map format in this implementation
        // Add if needed similar to ConfigurationSection parsing

        Map<String, String> nbt = new HashMap<>();

        return new DailyReward.RewardItem(material, amount, name, lore, enchantments, glow, nbt);
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
        rewards.clear();
        rewards.putAll(loadRewards());
    }
}