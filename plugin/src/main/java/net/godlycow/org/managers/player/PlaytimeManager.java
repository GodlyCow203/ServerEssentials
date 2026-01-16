package net.godlycow.org.managers.player;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import net.godlycow.org.EssC;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class PlaytimeManager {

    private final EssC plugin;
    private final File playtimeFile;
    private final FileConfiguration playtimeConfig;
    public PlaytimeManager(EssC plugin) {
        this.plugin = plugin;

        File pluginFolder = plugin.getDataFolder();
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs();
        }

        File storageFolder = new File(pluginFolder, "storage");
        if (!storageFolder.exists()) {
            storageFolder.mkdirs();
        }

        this.playtimeFile = new File(storageFolder, "PlayTime.yml");

        if (!playtimeFile.exists()) {
            try {
                playtimeFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.playtimeConfig = YamlConfiguration.loadConfiguration(playtimeFile);

        loadOnlinePlayers();
    }


    public void savePlaytime(UUID uuid, long seconds) {
        playtimeConfig.set(uuid.toString(), seconds);
        saveFile();
    }

    public long getPlaytime(UUID uuid) {
        return playtimeConfig.getLong(uuid.toString(), 0L);
    }

    public void updatePlaytime(Player player) {
        long ticks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        long seconds = ticks / 20;
        savePlaytime(player.getUniqueId(), seconds);

        Bukkit.getLogger().info("Saved playtime for " + player.getName() + ": " + seconds + "s");
    }

    public void updatePlaytime(OfflinePlayer player) {
        if (player.isOnline() && player.getPlayer() != null) {
            updatePlaytime(player.getPlayer());
        }
    }

    public List<Map.Entry<UUID, Long>> getTopPlaytimes(int top) {
        Map<UUID, Long> sorted = new HashMap<>();

        for (String key : playtimeConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                long time = playtimeConfig.getLong(key);
                sorted.put(uuid, time);
            } catch (IllegalArgumentException ignored) {}
        }

        return sorted.entrySet().stream()
                .sorted(Map.Entry.<UUID, Long>comparingByValue().reversed())
                .limit(top)
                .collect(Collectors.toList());
    }

    private void saveFile() {
        try {
            playtimeConfig.save(playtimeFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlaytime(player);
        }
    }
}
