package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class RTPConfig {

    private static final File file = new File("plugins/ServerEssentials", "rtpconfig.yml");
    private static FileConfiguration config;

    public static void load() {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
                config = YamlConfiguration.loadConfiguration(file);

                for (World world : Bukkit.getWorlds()) {
                    String name = world.getName();
                    config.set(name + ".enabled", true);
                    config.set(name + ".cooldown", 60);
                    config.set(name + ".min-radius", 500);
                    config.set(name + ".max-radius", 2000);
                }

                config.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            config = YamlConfiguration.loadConfiguration(file);
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
