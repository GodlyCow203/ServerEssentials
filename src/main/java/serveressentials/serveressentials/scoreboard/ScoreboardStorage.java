package serveressentials.serveressentials.scoreboard;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ScoreboardStorage {

    private final File file;
    private YamlConfiguration config;

    // ✅ Cache disabled/enabled states in memory to avoid timing issues
    private final Map<UUID, Boolean> scoreboardStates = new HashMap<>();

    public ScoreboardStorage(JavaPlugin plugin) {
        this.file = new File(plugin.getDataFolder(), "storage/scoreboard.yml");
        reload();
        loadCachedStates();
    }

    public void reload() {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        loadCachedStates();
    }

    /** ✅ Load player states into memory for quick access */
    private void loadCachedStates() {
        scoreboardStates.clear();
        if (config.isConfigurationSection("players")) {
            for (String uuidStr : config.getConfigurationSection("players").getKeys(false)) {
                boolean enabled = config.getBoolean("players." + uuidStr + ".enabled", true);
                try {
                    scoreboardStates.put(UUID.fromString(uuidStr), enabled);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    /** ✅ Toggle scoreboard for player */
    public boolean togglePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        boolean newState = !isEnabled(player);

        scoreboardStates.put(uuid, newState);
        config.set("players." + uuid + ".enabled", newState);
        save();

        return newState;
    }

    /** ✅ Check if scoreboard is enabled for player (cached in memory) */
    public boolean isEnabled(Player player) {
        return scoreboardStates.getOrDefault(player.getUniqueId(), true);
    }

    public void setPlayerLayout(Player player, String layout) {
        config.set("players." + player.getUniqueId() + ".layout", layout);
        save();
    }

    public String getPlayerLayout(Player player) {
        return config.getString("players." + player.getUniqueId() + ".layout", "default");
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
