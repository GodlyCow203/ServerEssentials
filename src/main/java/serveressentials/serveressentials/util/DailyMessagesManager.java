package serveressentials.serveressentials.util;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DailyMessagesManager {

    private static DailyMessagesManager instance; // ✅ static instance for global access
    private final JavaPlugin plugin;
    private File file;
    private FileConfiguration config;

    public DailyMessagesManager(JavaPlugin plugin) {
        this.plugin = plugin;
        instance = this;
        loadConfig();
    }

    public static DailyMessagesManager getInstance() {
        return instance;
    }

    /**
     * Reloads the daily.yml file from disk.
     * Automatically recreates it if missing.
     */
    public void reload() {
        if (file == null) {
            loadConfig();
            return;
        }

        if (!file.exists()) {
            try (InputStream in = plugin.getResource("messages/daily.yml")) {
                if (in != null) java.nio.file.Files.copy(in, file.toPath());
            } catch (IOException e) {
                Bukkit.getLogger().severe("[ServerEssentials] Failed to recreate daily.yml!");
                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
        Bukkit.getLogger().info("[ServerEssentials] DailyMessagesManager reloaded successfully.");
    }

    /**
     * Global static reload shortcut.
     */
    public static void fullReload() {
        if (instance == null) {
            Bukkit.getLogger().warning("[ServerEssentials] Tried to reload DailyMessagesManager before initialization!");
            return;
        }
        instance.reload();
    }

    private void loadConfig() {
        File folder = new File(plugin.getDataFolder(), "messages");
        if (!folder.exists()) folder.mkdirs();

        file = new File(folder, "daily.yml");
        if (!file.exists()) {
            try (InputStream in = plugin.getResource("messages/daily.yml")) {
                if (in != null) java.nio.file.Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public List<Map<String, Object>> getRewardItems(String day) {
        List<Map<?, ?>> raw = config.getMapList("rewards." + day + ".items");
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<?, ?> map : raw) {
            list.add((Map<String, Object>) map);
        }
        return list;
    }

    public String get(String path, Object... placeholders) {
        String msg = config.getString(path, "<red>Message not found");
        for (int i = 0; i < placeholders.length - 1; i += 2) {
            msg = msg.replace(placeholders[i].toString(), placeholders[i + 1].toString());
        }
        return msg;
    }

    public List<String> getConfigSection(String path) {
        if (config.getConfigurationSection(path) == null) return List.of();
        return List.copyOf(config.getConfigurationSection(path).getKeys(false));
    }

    public int getTotalPages(List<String> keys) {
        int pages = 1;
        for (String day : keys) {
            int page = config.getInt("rewards." + day + ".page", 1);
            pages = Math.max(pages, page);
        }
        return pages;
    }

    public int getRewardPage(String day) {
        return config.getInt("rewards." + day + ".page", 1);
    }

    public int getRewardSlot(String day) {
        return config.getInt("rewards." + day + ".slot", 0);
    }

    public Map<String, Object> getRewardItem(String day) {
        List<Map<?, ?>> list = config.getMapList("rewards." + day + ".items");
        return list.isEmpty() ? Map.of() : (Map<String, Object>) list.get(0);
    }
}
