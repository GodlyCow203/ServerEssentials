package serveressentials.serveressentials.listeners;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class KillTracker implements Listener {

    private final File file;
    private final FileConfiguration config;

    public KillTracker(JavaPlugin plugin) {
        // Ensure plugin's folder exists: /plugins/ServerEssentials
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // Create /plugins/ServerEssentials/storage/ folder
        File storageFolder = new File(plugin.getDataFolder(), "storage");
        if (!storageFolder.exists()) {
            storageFolder.mkdirs();
        }

        // Create kills.yml inside /plugins/ServerEssentials/storage/
        this.file = new File(storageFolder, "kills.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.config = YamlConfiguration.loadConfiguration(file);

        // Register events
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }



    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player)) return;

        Player killer = event.getEntity().getKiller();
        UUID uuid = killer.getUniqueId();
        String path = uuid.toString();

        int kills = config.getInt(path, 0) + 1;
        config.set(path, kills);

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Optional: notify player
        // killer.sendMessage("§aKill recorded! You now have §e" + kills + "§a kills.");
    }

    public int getKills(Player player) {
        return config.getInt(player.getUniqueId().toString(), 0);
    }
}
