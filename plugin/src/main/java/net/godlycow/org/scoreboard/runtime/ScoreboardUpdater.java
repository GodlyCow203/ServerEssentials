package net.godlycow.org.scoreboard.runtime;

import net.godlycow.org.scoreboard.storage.ScoreboardStorage;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.godlycow.org.commands.config.ScoreboardConfig;
import net.godlycow.org.scoreboard.util.PlaceholderUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class ScoreboardUpdater {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final int MAX_LINES = 15;
    private static final long PLACEHOLDER_CACHE_TTL = TimeUnit.SECONDS.toMillis(1);

    private final Plugin plugin;
    private final ScoreboardConfig config;
    private final ScoreboardStorage storage;
    private final Map<UUID, Scoreboard> playerBoards = new ConcurrentHashMap<>();
    private final Map<UUID, Objective> playerObjectives = new ConcurrentHashMap<>();
    private final Map<UUID, Map<Integer, Team>> playerTeams = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, String>> placeholderCache = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastPlaceholderUpdate = new ConcurrentHashMap<>();
    private final boolean debugMode;
    private static final String INVISIBLE_CHAR = "§";

    public ScoreboardUpdater(Plugin plugin, ScoreboardConfig config, ScoreboardStorage storage) {
        this.plugin = plugin;
        this.config = config;
        this.storage = storage;
        this.debugMode = plugin.getConfig().getBoolean("scoreboard.debug-mode", false);
        startTask();
        plugin.getLogger().info("Scoreboard updater started in " + (debugMode ? "debug" : "production") + " mode");
    }

    private void startTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin,
                () -> {
                    if (!config.enabled) {
                        return;
                    }
                    Bukkit.getOnlinePlayers().forEach(this::updateAsync);
                },
                20L, config.updateInterval
        );
    }

    public void updateAsync(Player player) {
        if (!config.enabled) {
            Bukkit.getScheduler().runTask(plugin, () -> clear(player));
            return;
        }

        if (!storage.isEnabled(player.getUniqueId())) {
            Bukkit.getScheduler().runTask(plugin, () -> clear(player));
            return;
        }

        String layout = config.getLayoutForPlayer(player, storage);
        Bukkit.getScheduler().runTask(plugin, () -> update(player, layout));
    }

    public void refreshAll() {
        if (!config.enabled) return;

        Bukkit.getOnlinePlayers().forEach(this::updateAsync);
    }

    public void clear(Player player) {
        UUID uuid = player.getUniqueId();
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        playerBoards.remove(uuid);
        playerObjectives.remove(uuid);
        playerTeams.remove(uuid);
        placeholderCache.remove(uuid);
        lastPlaceholderUpdate.remove(uuid);
    }

    public void update(Player player) {
        if (!config.enabled) {
            clear(player);
            return;
        }

        if (!storage.isEnabled(player.getUniqueId())) {
            clear(player);
            return;
        }
        String layout = config.getLayoutForPlayer(player, storage);
        update(player, layout);
    }

    public void update(Player player, String layout) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, () -> update(player, layout));
            return;
        }

        if (!config.enabled) {
            clear(player);
            return;
        }

        if (!storage.isEnabled(player.getUniqueId())) {
            clear(player);
            return;
        }

        String world = player.getWorld().getName();
        if (!config.isWorldEnabled(world)) {
            clear(player);
            return;
        }

        UUID uuid = player.getUniqueId();
        ScoreboardConfig.Layout layoutData = config.getLayout(layout);

        Scoreboard board = playerBoards.computeIfAbsent(uuid,
                k -> Bukkit.getScoreboardManager().getNewScoreboard());

        Objective obj = playerObjectives.computeIfAbsent(uuid, k -> {
            Objective o = board.registerNewObjective("se_sb", "dummy",
                    MINI_MESSAGE.deserialize(layoutData.title()));
            o.setDisplaySlot(DisplaySlot.SIDEBAR);
            return o;
        });

        obj.displayName(MINI_MESSAGE.deserialize(layoutData.title()));

        Map<Integer, Team> teams = playerTeams.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());
        Map<String, String> cache = placeholderCache.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());
        long lastUpdate = lastPlaceholderUpdate.getOrDefault(uuid, 0L);
        long now = System.currentTimeMillis();

        if (now - lastUpdate > PLACEHOLDER_CACHE_TTL) {
            cache.clear();
            lastPlaceholderUpdate.put(uuid, now);
        }

        List<String> lines = layoutData.lines();
        int visibleLines = Math.min(lines.size(), layoutData.maxLines());

        for (int i = 0; i < visibleLines; i++) {
            String line = lines.get(i);
            String cachedLine = cache.get(line);

            if (cachedLine == null) {
                cachedLine = PlaceholderUtil.apply(player, line);
                cache.put(line, cachedLine);
            }

            String entry = INVISIBLE_CHAR + (char)('a' + i);

            int finalI = i;
            Team team = teams.computeIfAbsent(i, k -> {
                Team t = board.registerNewTeam("se_line_" + uuid + "_" + finalI);
                t.addEntry(entry);
                return t;
            });

            team.prefix(MINI_MESSAGE.deserialize(cachedLine));
            obj.getScore(entry).setScore(visibleLines - i);
        }

        for (int i = visibleLines; i < MAX_LINES; i++) {
            Team team = teams.remove(i);
            if (team != null) team.unregister();
        }

        if (player.getScoreboard() != board) {
            player.setScoreboard(board);
        }
    }
}