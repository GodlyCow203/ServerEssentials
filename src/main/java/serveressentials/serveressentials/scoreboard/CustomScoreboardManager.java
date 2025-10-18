package serveressentials.serveressentials.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import serveressentials.serveressentials.scoreboard.util.MessageUtil;

public class CustomScoreboardManager {

    private static CustomScoreboardManager instance;

    private final JavaPlugin plugin;
    private final ScoreboardConfig configHandler;
    private final ScoreboardStorage storage;
    private final ScoreboardDatabase database;
    private final ScoreboardUpdater updater;
    private final MessageUtil messages;

    public CustomScoreboardManager(JavaPlugin plugin) {
        instance = this;

        this.plugin = plugin;
        this.messages = new MessageUtil(plugin, "messages/scoreboard_system.yml");
        this.configHandler = new ScoreboardConfig(plugin);
        this.storage = new ScoreboardStorage(plugin);
        this.database = new ScoreboardDatabase(plugin);
        this.updater = new ScoreboardUpdater(this);

        plugin.getCommand("scoreboard").setExecutor(new ScoreboardCommand(this));
        Bukkit.getPluginManager().registerEvents(new ScoreboardListener(this), plugin);

        updater.start();
    }

    public static CustomScoreboardManager getInstance() {
        return instance;
    }

    /** 🔁 Fully reloads everything and reapplies scoreboards */
    public static void reloadAll() {
        if (instance == null) return;

        instance.configHandler.reload();
        instance.storage.reload();
        instance.messages.reload();

        // Apply updated scoreboard to everyone
        instance.updater.refreshAll();

        instance.plugin.getLogger().info("[Scoreboard] Fully reloaded configuration and refreshed all player scoreboards.");
    }

    public void reload() {
        reloadAll();
    }

    public JavaPlugin getPlugin() { return plugin; }
    public ScoreboardConfig getConfigHandler() { return configHandler; }
    public ScoreboardStorage getStorage() { return storage; }
    public ScoreboardDatabase getDatabase() { return database; }
    public ScoreboardUpdater getUpdater() { return updater; }
    public MessageUtil getMessages() { return messages; }
}
