package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class WarpManager {

    private static File file;
    private static FileConfiguration config;
    static final Map<String, WarpData> warps = new HashMap<>();
    private static final Map<String, Map<String, Long>> warpCooldowns = new HashMap<>();

    public static void setup() {
        file = new File(Bukkit.getPluginManager().getPlugin("ServerEssentials").getDataFolder(), "warps.yml");
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        loadWarps();
    }

    private static void loadWarps() {
        warps.clear();
        if (!config.contains("warps")) return;

        for (String name : config.getConfigurationSection("warps").getKeys(false)) {
            String path = "warps." + name;
            Location loc = config.getLocation(path + ".location");
            boolean enabled = config.getBoolean(path + ".enabled", true);
            String category = config.getString(path + ".category", "default");
            Material material = Material.matchMaterial(config.getString(path + ".material", "ENDER_PEARL"));
            List<String> lore = config.getStringList(path + ".lore");
            long cooldown = config.getLong(path + ".cooldown", 60);

            if (loc != null && material != null) {
                warps.put(name.toLowerCase(), new WarpData(name, loc, enabled, category, material, lore, cooldown));
            }
        }
    }

    private static void saveWarps() {
        config.set("warps", null);
        for (Map.Entry<String, WarpData> entry : warps.entrySet()) {
            WarpData data = entry.getValue();
            String path = "warps." + data.getName().toLowerCase();
            config.set(path + ".location", data.getLocation());
            config.set(path + ".enabled", data.isEnabled());
            config.set(path + ".category", data.getCategory());
            config.set(path + ".material", data.getMaterial().name());
            config.set(path + ".lore", data.getLore());
            config.set(path + ".cooldown", data.getCooldown());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveWarp(WarpData data) {
        warps.put(data.getName().toLowerCase(), data);
        saveWarps();
    }

    public static void setWarp(String name, Location loc) {
        WarpData data = new WarpData(name, loc);
        warps.put(name.toLowerCase(), data);
        saveWarp(data);
    }

    public static boolean warpExists(String name) {
        return warps.containsKey(name.toLowerCase());
    }

    public static WarpData getWarpData(String name) {
        return warps.get(name.toLowerCase());
    }

    public static Location getWarp(String name) {
        WarpData data = getWarpData(name);
        return data != null ? data.getLocation() : null;
    }

    public static boolean isWarpEnabled(String name) {
        WarpData data = getWarpData(name);
        return data != null && data.isEnabled();
    }

    public static void closeWarp(String name) {
        WarpData data = getWarpData(name);
        if (data != null) {
            data.setEnabled(false);
            saveWarp(data);
        }
    }

    public static void reopenWarp(String name) {
        WarpData data = getWarpData(name);
        if (data != null) {
            data.setEnabled(true);
            saveWarp(data);
        }
    }

    public static void deleteWarp(String name) {
        warps.remove(name.toLowerCase());
        config.set("warps." + name.toLowerCase(), null);
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void renameWarp(String oldName, String newName) {
        WarpData data = warps.remove(oldName.toLowerCase());
        if (data != null) {
            data.setName(newName);
            warps.put(newName.toLowerCase(), data);
            config.set("warps." + oldName.toLowerCase(), null);
            saveWarp(data);
        }
    }

    public static void setWarpMaterial(String name, Material mat) {
        WarpData data = getWarpData(name);
        if (data != null) {
            data.setMaterial(mat);
            saveWarp(data);
        }
    }
    public static List<String> getAllWarpNames() {
        return new ArrayList<>(warps.keySet());
    }


    public static void setWarpDescription(String warpName, String description) {
        if (warps.containsKey(warpName)) {
            warps.get(warpName).setDescription(description);
        }
    }


    public static void setWarpCooldown(String name, int seconds) {
        WarpData data = getWarpData(name);
        if (data != null) {
            data.setCooldown(seconds);
            saveWarp(data);
        }
    }

    public static void setWarpCategory(String name, String category) {
        List<String> allowed = ServerEssentials.getInstance().getConfig().getStringList("allowed-categories");
        if (!allowed.contains(category.toLowerCase())) {
            Bukkit.getLogger().warning("[ServerEssentials] Category '" + category + "' is not allowed!");
            return;
        }

        WarpData data = getWarpData(name);
        if (data != null) {
            data.setCategory(category);
            saveWarp(data);
        }
    }

    public static List<String> getWarpNames() {
        return new ArrayList<>(warps.keySet());
    }

    public static List<String> getAllCategories() {
        return warps.values().stream()
                .map(WarpData::getCategory)
                .distinct()
                .collect(Collectors.toList());
    }

    public static List<WarpData> getWarpsByCategory(String category) {
        return warps.values().stream()
                .filter(w -> w.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    public static boolean isOnCooldown(UUID uuid, String warp) {
        Map<String, Long> playerCooldowns = warpCooldowns.getOrDefault(uuid.toString(), new HashMap<>());
        long now = System.currentTimeMillis();
        return playerCooldowns.getOrDefault(warp.toLowerCase(), 0L) > now;
    }

    public static long getCooldownRemaining(UUID uuid, String warp) {
        Map<String, Long> playerCooldowns = warpCooldowns.getOrDefault(uuid.toString(), new HashMap<>());
        long now = System.currentTimeMillis();
        long expires = playerCooldowns.getOrDefault(warp.toLowerCase(), 0L);
        return Math.max(0, expires - now);
    }
    public static Collection<WarpData> getAllWarps() {
        return warps.values();
    }



    public static void setCooldown(UUID uuid, String warp, int seconds) {
        warpCooldowns.computeIfAbsent(uuid.toString(), k -> new HashMap<>())
                .put(warp.toLowerCase(), System.currentTimeMillis() + seconds * 1000L);
    }
}