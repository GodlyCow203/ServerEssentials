package net.lunark.io.scoreboard;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import net.lunark.io.scoreboard.util.PlaceholderUtil;

import java.util.List;

public class ScoreboardUpdater {

    private final CustomScoreboardManager manager;
    private final MiniMessage mini = MiniMessage.miniMessage();

    public ScoreboardUpdater(CustomScoreboardManager manager) {
        this.manager = manager;
    }

    public void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (manager.getStorage().isEnabled(player)) {
                        update(player); // auto layout
                    } else {
                        clear(player);
                    }
                }
            }
        }.runTaskTimer(manager.getPlugin(), 20L, 20L); // 1 second
    }
    public void refreshAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (manager.getStorage().isEnabled(player)) {
                update(player);
            } else {
                clear(player);
            }
        }
    }

    public void clear(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    public void update(Player player) {
        if (!manager.getStorage().isEnabled(player)) {
            clear(player);
            return;
        }
        String layout = manager.getConfigHandler().getLayoutForPlayer(player, manager.getStorage());
        update(player, layout);
    }

    public void update(Player player, String layout) {
        if (!manager.getStorage().isEnabled(player)) {
            clear(player);
            return;
        }

        String world = player.getWorld().getName();
        if (!manager.getConfigHandler().isWorldEnabled(world)) {
            clear(player);
            return;
        }

        String title = PlaceholderUtil.apply(player, manager.getConfigHandler().getTitle(layout));
        List<String> lines = manager.getConfigHandler().getLines(layout);

        Scoreboard board = player.getScoreboard();
        if (board == Bukkit.getScoreboardManager().getMainScoreboard() || board == null) {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
        }

        Objective obj = board.getObjective("sidebar");
        if (obj == null) {
            obj = board.registerNewObjective("sidebar", "dummy", mini.deserialize(title));
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        } else {
            obj.displayName(mini.deserialize(title));
        }

        // === FIX: Completely wipe ALL old entries and teams ===
        for (String entry : board.getEntries()) {
            if (entry.startsWith("§")) {
                board.resetScores(entry);
                Team t = board.getTeam("line" + entry.substring(1));
                if (t != null) {
                    t.unregister();
                }
            }
        }

        // === Add fresh lines with hidden scores ===
        for (int i = 0; i < lines.size(); i++) {
            String replaced = PlaceholderUtil.apply(player, lines.get(i));
            String entry = "§" + i;

            Team team = board.registerNewTeam("line" + i);
            team.addEntry(entry);
            team.prefix(mini.deserialize(replaced));
            obj.getScore(entry).setScore(0); // Hide numbers
        }

        player.setScoreboard(board);
    }
}
