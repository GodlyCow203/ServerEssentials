package serveressentials.serveressentials.config;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class RTPConfig {

    private static final File file = new File("plugins/ServerEssentials/config/rtp", "rtpconfig.yml");
    private static FileConfiguration config;

    public static void load() {
        ensureFileExists();
        config = YamlConfiguration.loadConfiguration(file);
        addDefaultsForMissingWorlds();
        save();
    }

    public static void reload() {
        if (!file.exists()) {
            Bukkit.getLogger().warning("[RTPConfig] Config missing, recreating...");
            load();
            return;
        }
        config = YamlConfiguration.loadConfiguration(file);
        addDefaultsForMissingWorlds();
        save();
        Bukkit.getLogger().info("[RTPConfig] Reloaded successfully.");
    }

    private static void ensureFileExists() {
        File parent = file.getParentFile();
        if (!parent.exists()) parent.mkdirs();

        if (!file.exists()) {
            try {
                file.createNewFile();
                Bukkit.getLogger().info("[RTPConfig] Created new rtpconfig.yml");
            } catch (IOException e) {
                Bukkit.getLogger().severe("[RTPConfig] Failed to create config file!");
                e.printStackTrace();
            }
        }
    }

    private static void addDefaultsForMissingWorlds() {
        for (World world : Bukkit.getWorlds()) {
            String name = world.getName();
            boolean updated = false;

            if (!config.contains(name + ".enabled")) {
                config.set(name + ".enabled", true);
                updated = true;
            }
            if (!config.contains(name + ".cooldown")) {
                config.set(name + ".cooldown", 60);
                updated = true;
            }
            if (!config.contains(name + ".min-radius")) {
                config.set(name + ".min-radius", 500);
                updated = true;
            }
            if (!config.contains(name + ".max-radius")) {
                config.set(name + ".max-radius", 2000);
                updated = true;
            }

            if (updated) {
                Bukkit.getLogger().info("[RTPConfig] Added defaults for world: " + name);
            }
        }
    }

    public static void fullReload() {
        reload();
    }

    public static void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().severe("[RTPConfig] Failed to save!");
            e.printStackTrace();
        }
    }

    public static boolean isEnabled(String world) {
        return config.getBoolean(world + ".enabled", true);
    }

    public static int getCooldown(String world) {
        return config.getInt(world + ".cooldown", 60);
    }

    public static int getMinRadius(String world) {
        return config.getInt(world + ".min-radius", 500);
    }

    public static int getMaxRadius(String world) {
        return config.getInt(world + ".max-radius", 2000);
    }
}
