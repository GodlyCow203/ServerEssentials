package serveressentials.serveressentials.pw;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import serveressentials.serveressentials.config.GUIConfig;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class WarpStorage {

    private final Map<UUID, List<PlayerWarp>> warpMap = new HashMap<>();
    private final Map<UUID, PlayerWarp> editing = new HashMap<>();
    private final GUIConfig guiConfig;

    private final File file;
    private FileConfiguration config;

    // Default hardcoded categories
    public static final List<String> HARDCODED_CATEGORIES = List.of(
            "Shop", "Server", "Misc", "Builds", "Towns", "Redstone"
    );

    /**
     * Constructor to handle warp storage with persistent YAML
     */
    public WarpStorage(JavaPlugin plugin, GUIConfig guiConfig) {
        this.guiConfig = guiConfig;

        // Ensure storage folder exists
        File storageFolder = new File(plugin.getDataFolder(), "storage");
        if (!storageFolder.exists()) {
            storageFolder.mkdirs();
        }

        // File under storage/pw.yml
        this.file = new File(storageFolder, "pw.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create storage/pw.yml!");
                e.printStackTrace();
            }
        }

        this.config = YamlConfiguration.loadConfiguration(file);
    }


    /**
     * Loads all warps from pw.yml into memory
     */
    public void loadAll() {
        warpMap.clear();

        if (!config.contains("warps")) {
            return; // No warps to load
        }

        for (String uuidStr : config.getConfigurationSection("warps").getKeys(false)) {
            UUID owner = UUID.fromString(uuidStr);
            List<PlayerWarp> playerWarps = new ArrayList<>();

            for (String warpName : config.getConfigurationSection("warps." + uuidStr).getKeys(false)) {
                String basePath = "warps." + uuidStr + "." + warpName;

                String worldName = config.getString(basePath + ".world");
                World world = Bukkit.getWorld(worldName);

                if (world == null) {
                    Bukkit.getLogger().warning("Warp '" + warpName + "' skipped: world '" + worldName + "' does not exist.");
                    continue;
                }

                double x = config.getDouble(basePath + ".x");
                double y = config.getDouble(basePath + ".y");
                double z = config.getDouble(basePath + ".z");
                float yaw = (float) config.getDouble(basePath + ".yaw");
                float pitch = (float) config.getDouble(basePath + ".pitch");
                String category = config.getString(basePath + ".category", "Misc");

                Location loc = new Location(world, x, y, z, yaw, pitch);
                playerWarps.add(new PlayerWarp(owner, warpName, loc, category));
            }

            warpMap.put(owner, playerWarps);
        }

    }

    /**
     * Saves all warps from memory to pw.yml
     */
    public void saveAll() {
        config.set("warps", null); // Clear old data

        for (UUID owner : warpMap.keySet()) {
            for (PlayerWarp warp : warpMap.get(owner)) {
                String basePath = "warps." + owner.toString() + "." + warp.getName();
                Location loc = warp.getLocation();

                config.set(basePath + ".world", loc.getWorld().getName());
                config.set(basePath + ".x", loc.getX());
                config.set(basePath + ".y", loc.getY());
                config.set(basePath + ".z", loc.getZ());
                config.set(basePath + ".yaw", loc.getYaw());
                config.set(basePath + ".pitch", loc.getPitch());
                config.set(basePath + ".category", warp.getCategory());
            }
        }

        try {
            config.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to save pw.yml!");
            e.printStackTrace();
        }
    }

    /**
     * Add a new warp for a player
     */
    public void addWarp(PlayerWarp warp) {
        warpMap.computeIfAbsent(warp.getOwner(), k -> new ArrayList<>()).add(warp);
        saveAll(); // Auto-save after adding a warp
    }

    /**
     * Get all warps owned by a player
     */
    public List<PlayerWarp> getWarps(UUID playerId) {
        return warpMap.getOrDefault(playerId, new ArrayList<>());
    }

    /**
     * Get available categories from GUI config
     */
    public List<String> getCategories(UUID player) {
        return guiConfig.getCategories();
    }

    /**
     * Set a warp as currently being edited
     */
    public void setEditingWarp(Player player, PlayerWarp warp) {
        editing.put(player.getUniqueId(), warp);
    }

    /**
     * Get the warp currently being edited by a player
     */
    public PlayerWarp getEditingWarp(Player player) {
        return editing.get(player.getUniqueId());
    }

    /**
     * Clear the warp being edited
     */
    public void clearEditingWarp(Player player) {
        editing.remove(player.getUniqueId());
    }

    /**
     * Get all warps inside a specific category, across all players
     */
    public List<PlayerWarp> getWarpsInCategory(String category) {
        List<PlayerWarp> result = new ArrayList<>();
        for (List<PlayerWarp> warps : warpMap.values()) {
            for (PlayerWarp warp : warps) {
                if (warp.getCategory().equalsIgnoreCase(category)) {
                    result.add(warp);
                }
            }
        }
        return result;
    }

    /**
     * Get a specific warp by name, regardless of owner
     */
    public PlayerWarp getWarp(String warpName) {
        for (List<PlayerWarp> warps : warpMap.values()) {
            for (PlayerWarp warp : warps) {
                if (warp.getName().equalsIgnoreCase(warpName)) {
                    return warp;
                }
            }
        }
        return null;
    }
}
