package serveressentials.serveressentials.Managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class RulesManager {

    private static RulesManager instance;

    private final JavaPlugin plugin;
    private final MiniMessage mini = MiniMessage.miniMessage();

    private File rulesFile;
    private FileConfiguration rulesConfig;

    public RulesManager(JavaPlugin plugin) {
        instance = this;
        this.plugin = plugin;
        loadOrCreateRules();
    }

    /**
     * Get the active instance of RulesManager.
     */
    public static RulesManager getInstance() {
        return instance;
    }

    /**
     * Reload the rules configuration statically.
     */
    public static void reload() {
        if (instance != null) {
            instance.reloadRules();
        } else {
            System.out.println("[RulesManager] Tried to reload before initialization!");
        }
    }

    /**
     * Loads or creates the rules.yml file inside /config/rules/
     */
    private void loadOrCreateRules() {
        File configFolder = new File(plugin.getDataFolder(), "config/rules");
        if (!configFolder.exists()) {
            configFolder.mkdirs();
        }

        rulesFile = new File(configFolder, "rules.yml");

        if (!rulesFile.exists()) {
            try {
                rulesFile.createNewFile();
                rulesConfig = YamlConfiguration.loadConfiguration(rulesFile);

                List<String> defaultRules = Arrays.asList(
                        "<green>- Be respectful to others.",
                        "<red>- No griefing or stealing.",
                        "<yellow>- Keep chat appropriate.",
                        "<aqua>- No cheating or using exploits."
                );

                rulesConfig.set("rules", defaultRules);
                rulesConfig.save(rulesFile);
                plugin.getLogger().info("Created new rules.yml with default rules.");

            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create rules.yml: " + e.getMessage());
            }
        } else {
            rulesConfig = YamlConfiguration.loadConfiguration(rulesFile);
        }
    }

    /**
     * Returns a list of MiniMessage-formatted rules.
     */
    public List<String> getRules() {
        return rulesConfig.getStringList("rules");
    }

    /**
     * Sends all rules to a player or console sender with MiniMessage formatting.
     */
    public void sendRules(CommandSender sender) {
        sender.sendMessage(mini.deserialize("<gold><bold>Server Rules:</bold></gold>"));
        for (String rule : getRules()) {
            sender.sendMessage(mini.deserialize(rule));
        }
    }

    /**
     * Reload the rules configuration from disk.
     */
    public void reloadRules() {
        rulesConfig = YamlConfiguration.loadConfiguration(rulesFile);
        plugin.getLogger().info("Rules configuration reloaded.");
    }

    /**
     * Save the current rules to disk.
     */
    public void saveRules() {
        try {
            rulesConfig.save(rulesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save rules.yml: " + e.getMessage());
        }
    }
}
