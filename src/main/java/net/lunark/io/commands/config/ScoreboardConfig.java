package net.lunark.io.commands.config;

import net.lunark.io.scoreboard.ScoreboardStorage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.logging.Level;

public final class ScoreboardConfig {
    public final boolean enabled;
    public final int updateInterval;
    public final String defaultLayout;
    public final Map<String, Layout> layouts = new HashMap<>();
    public final Map<String, WorldSetting> worldSettings = new HashMap<>();

    public record Layout(String title, List<String> lines, int maxLines) {}
    public record WorldSetting(boolean enabled, String layout) {}

    public ScoreboardConfig(Plugin plugin) {
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("scoreboard");
        if (config == null) {
            plugin.getLogger().warning("Missing 'scoreboard' section in config.yml, using defaults");
            this.enabled = true;
            this.updateInterval = 20;
            this.defaultLayout = "default";
            return;
        }

        this.enabled = config.getBoolean("enabled", true);
        this.updateInterval = Math.max(1, config.getInt("update-interval", 20));
        this.defaultLayout = config.getString("default-layout", "default");

        loadLayouts(config.getConfigurationSection("layouts"), plugin);
        loadWorldSettings(config.getConfigurationSection("world-settings"), plugin);

        plugin.getLogger().info("Loaded " + layouts.size() + " scoreboard layouts");
    }

    private void loadLayouts(ConfigurationSection section, Plugin plugin) {
        if (section == null) {
            plugin.getLogger().warning("No layouts defined, using empty default");
            layouts.put("default", new Layout("<red>Scoreboard", List.of(), 15));
            return;
        }

        for (String name : section.getKeys(false)) {
            ConfigurationSection layoutSec = section.getConfigurationSection(name);
            if (layoutSec == null) continue;

            String title = layoutSec.getString("title", "<red>Scoreboard");
            List<String> lines = layoutSec.getStringList("lines");
            int maxLines = layoutSec.getInt("max-lines", 15);

            if (lines.isEmpty()) {
                plugin.getLogger().warning("Layout '" + name + "' has no lines defined");
            }

            layouts.put(name, new Layout(title, lines, Math.min(maxLines, 15)));
        }

        if (!layouts.containsKey(defaultLayout)) {
            plugin.getLogger().warning("Default layout '" + defaultLayout + "' not found, creating empty");
            layouts.put(defaultLayout, new Layout("<red>Scoreboard", List.of(), 15));
        }
    }

    private void loadWorldSettings(ConfigurationSection section, Plugin plugin) {
        if (section == null) return;

        for (String world : section.getKeys(false)) {
            ConfigurationSection worldSec = section.getConfigurationSection(world);
            if (worldSec == null) continue;

            boolean enabled = worldSec.getBoolean("enabled", true);
            String layout = worldSec.getString("layout");

            worldSettings.put(world, new WorldSetting(enabled, layout));
            plugin.getLogger().fine("Loaded scoreboard setting for world: " + world);
        }
    }

    public Set<String> getLayoutNames() {
        return layouts.keySet();
    }

    public boolean layoutExists(String name) {
        return layouts.containsKey(name);
    }

    public Layout getLayout(String name) {
        return layouts.getOrDefault(name, layouts.get(defaultLayout));
    }

    public WorldSetting getWorldSetting(String world) {
        return worldSettings.get(world);
    }

    public boolean isWorldEnabled(String world) {
        WorldSetting setting = getWorldSetting(world);
        return setting == null || setting.enabled();
    }

    public String getWorldLayout(String world) {
        WorldSetting setting = getWorldSetting(world);
        return setting != null ? setting.layout() : null;
    }

    public String getLayoutForPlayer(Player player, ScoreboardStorage storage) {
        UUID uuid = player.getUniqueId();
        String savedLayout = storage.getLayout(uuid);
        if (savedLayout != null && layoutExists(savedLayout)) return savedLayout;

        String worldLayout = getWorldLayout(player.getWorld().getName());
        if (worldLayout != null && layoutExists(worldLayout)) return worldLayout;

        return defaultLayout;
    }
}