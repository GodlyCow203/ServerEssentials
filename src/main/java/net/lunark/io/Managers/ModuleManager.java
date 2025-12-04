package net.lunark.io.Managers;

import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class ModuleManager {

    private final Plugin plugin;
    private final Map<String, Boolean> modules = new HashMap<>();

    public ModuleManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void loadFromConfig(org.bukkit.configuration.file.FileConfiguration cfg) {
        if (cfg.getConfigurationSection("modules") == null) return;
        for (String key : cfg.getConfigurationSection("modules").getKeys(false)) {
            modules.put(key.toLowerCase(), cfg.getBoolean("modules." + key + ".enabled", true));
        }
    }

    public boolean isEnabled(String moduleKey) {
        return modules.getOrDefault(moduleKey.toLowerCase(), true);
    }

    public void setEnabled(String moduleKey, boolean enabled) {
        modules.put(moduleKey.toLowerCase(), enabled);
        // Persisting would be caller's responsibility via GlobalConfigManager.set(...)
    }

    public Map<String, Boolean> snapshot() {
        return new HashMap<>(modules);
    }
}
