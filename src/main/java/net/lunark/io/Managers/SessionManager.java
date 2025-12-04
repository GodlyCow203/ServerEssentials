package net.lunark.io.Managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import net.lunark.io.ServerEssentials;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class SessionManager {

    private final ServerEssentials plugin;
    private final File file;
    private FileConfiguration config;

    private final HashMap<UUID, Long> currentSessions = new HashMap<>();
    private final HashMap<UUID, Long> longestSessions = new HashMap<>();

    public SessionManager(ServerEssentials plugin) {
        this.plugin = plugin;
        file = new File(plugin.getDataFolder(), "storage/session.yml");

        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
        loadLongestSessions();
    }

    private void loadLongestSessions() {
        for (String uuidStr : config.getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            longestSessions.put(uuid, config.getLong(uuidStr, 0));
        }
    }

    public void startSession(Player player) {
        currentSessions.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public void endSession(Player player) {
        UUID uuid = player.getUniqueId();
        Long startTime = currentSessions.get(uuid);
        if (startTime == null) return;

        long duration = System.currentTimeMillis() - startTime;
        currentSessions.remove(uuid);

        long previousLongest = longestSessions.getOrDefault(uuid, 0L);
        if (duration > previousLongest) {
            longestSessions.put(uuid, duration);
            save();
        }
    }

    public long getCurrentSession(Player player) {
        Long startTime = currentSessions.get(player.getUniqueId());
        if (startTime == null) return 0;
        return System.currentTimeMillis() - startTime;
    }

    public long getLongestSession(Player player) {
        UUID uuid = player.getUniqueId();
        long current = getCurrentSession(player); // current session duration
        long longest = longestSessions.getOrDefault(uuid, 0L);
        return Math.max(current, longest);
    }


    private void save() {
        for (UUID uuid : longestSessions.keySet()) {
            config.set(uuid.toString(), longestSessions.get(uuid));
        }

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
