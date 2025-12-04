package net.lunark.io.scoreboard;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ScoreboardConfig {

    private final File file;
    private final JavaPlugin plugin;
    private YamlConfiguration config;

    public ScoreboardConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "config/scoreboard/scoreboard.yml");

        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            plugin.saveResource("config/scoreboard/scoreboard.yml", false);
        }
        reload();
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(file);
    }

    public List<String> getLayouts() {
        if (!config.contains("layouts")) return new ArrayList<>();
        return new ArrayList<>(config.getConfigurationSection("layouts").getKeys(false));
    }

    public boolean isWorldEnabled(String world) {
        return config.getBoolean("world-settings." + world + ".enabled", true);
    }

    public boolean layoutExists(String layout) {
        return config.contains("layouts." + layout);
    }

    public String getTitle(String layout) {
        if (!layoutExists(layout)) layout = config.getString("global.default-layout", "default");
        return config.getString("layouts." + layout + ".title", "<red>Scoreboard");
    }

    public List<String> getLines(String layout) {
        if (!layoutExists(layout)) layout = config.getString("global.default-layout", "default");
        List<String> lines = config.getStringList("layouts." + layout + ".lines");
        if (lines == null || lines.isEmpty()) return List.of("<red>No lines set!");
        return lines;
    }

    public String getLayoutForPlayer(Player player, ScoreboardStorage storage) {
        String playerLayout = storage.getPlayerLayout(player);
        if (playerLayout != null && !playerLayout.isEmpty()) return playerLayout;

        String world = player.getWorld().getName();
        if (config.contains("world-settings." + world + ".layout")) {
            return config.getString("world-settings." + world + ".layout");
        }

        return config.getString("global.default-layout", "default");
    }



    public boolean isEnabledGlobal() {
        return config.getBoolean("global.enabled", true);
    }
}
