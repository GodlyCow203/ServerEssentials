package net.lunark.io.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ScoreboardStorage {

    private final File file;
    private YamlConfiguration config;

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

    private void loadCachedStates() {
        scoreboardStates.clear();

        // 1. read file
        if (config.isConfigurationSection("players")) {
            for (String uuidStr : config.getConfigurationSection("players").getKeys(false)) {
                boolean enabled = config.getBoolean("players." + uuidStr + ".enabled", true);
                try {
                    scoreboardStates.put(UUID.fromString(uuidStr), enabled);
                } catch (IllegalArgumentException ignored) {}
            }
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            UUID id = p.getUniqueId();
            scoreboardStates.putIfAbsent(id, true);
        }
        scoreboardStates.keySet().removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
    }

    public void setEnabled(Player player, boolean enabled) {
        UUID uuid = player.getUniqueId();
        scoreboardStates.put(uuid, enabled);
        config.set("players." + uuid + ".enabled", enabled);
        save();
    }


    void setCachedState(UUID uuid, boolean enabled) {
        scoreboardStates.put(uuid, enabled);
    }
    void removeCachedState(UUID uuid) {
        scoreboardStates.remove(uuid);
    }

    public boolean togglePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        boolean newState = !isEnabled(player);

        scoreboardStates.put(uuid, newState);
        config.set("players." + uuid + ".enabled", newState);
        save();

        return newState;
    }

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
