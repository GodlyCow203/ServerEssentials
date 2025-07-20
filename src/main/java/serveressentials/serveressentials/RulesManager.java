package serveressentials.serveressentials;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class RulesManager {

    private final JavaPlugin plugin;
    private File rulesFile;
    private FileConfiguration rulesConfig;

    public RulesManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadOrCreateRules();
    }

    private void loadOrCreateRules() {
        rulesFile = new File(plugin.getDataFolder(), "rules.yml");

        if (!rulesFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                rulesFile.createNewFile();
                rulesConfig = YamlConfiguration.loadConfiguration(rulesFile);

                List<String> defaultRules = Arrays.asList(
                        "&c1. Be respectful to others.",
                        "&e2. No griefing or stealing.",
                        "&b3. Keep chat appropriate.",
                        "&a4. No cheating or using exploits."
                );
                rulesConfig.set("rules", defaultRules);
                rulesConfig.save(rulesFile);

            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create rules.yml: " + e.getMessage());
            }
        } else {
            rulesConfig = YamlConfiguration.loadConfiguration(rulesFile);
        }
    }

    public List<String> getRules() {
        return rulesConfig.getStringList("rules");
    }

    public void reloadRules() {
        rulesConfig = YamlConfiguration.loadConfiguration(rulesFile);
    }

    public void saveRules() {
        try {
            rulesConfig.save(rulesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save rules.yml: " + e.getMessage());
        }
    }
}
