package serveressentials.serveressentials.scoreboard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import serveressentials.serveressentials.scoreboard.util.PlaceholderUtil;

import java.util.List;

public class ScoreboardUpdater {

    private final CustomScoreboardManager manager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacySection();

    public ScoreboardUpdater(CustomScoreboardManager manager) {
        this.manager = manager;
    }

    public void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    // ✅ Skip players who toggled off their scoreboard
                    if (!manager.getStorage().isEnabled(player)) {
                        continue;
                    }
                    update(player);
                }
            }
        }.runTaskTimer(manager.getPlugin(), 20L, 40L); // every 2 seconds
    }

    public void refreshAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!manager.getStorage().isEnabled(player)) continue; // ✅ respect toggle
            update(player);
        }
    }

    public void clear(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    /** Update scoreboard for player using resolved layout (player → world → global) */
    public void update(Player player) {
        if (!manager.getStorage().isEnabled(player)) return; // ✅ respect toggle

        String layout = manager.getConfigHandler().getLayoutForPlayer(player, manager.getStorage());
        update(player, layout);
    }

    /** Update scoreboard using a specific layout */
    public void update(Player player, String layout) {
        if (!manager.getStorage().isEnabled(player)) return; // ✅ respect toggle

        String world = player.getWorld().getName();
        if (!manager.getConfigHandler().isWorldEnabled(world)) return;

        // Title
        String rawTitle = PlaceholderUtil.apply(player, manager.getConfigHandler().getTitle(layout));
        Component titleComponent = miniMessage.deserialize(rawTitle);

        List<String> rawLines = manager.getConfigHandler().getLines(layout);

        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("sidebar", "dummy", legacySerializer.serialize(titleComponent));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        int score = rawLines.size();
        int index = 0;

        for (String line : rawLines) {
            String replaced = PlaceholderUtil.apply(player, line);
            Component lineComponent = miniMessage.deserialize(replaced);

            String entryKey = "§" + index;

            Team team = board.getTeam("line" + index);
            if (team == null) team = board.registerNewTeam("line" + index);
            if (!team.hasEntry(entryKey)) team.addEntry(entryKey);

            String legacy = legacySerializer.serialize(lineComponent);
            String[] parts = splitLegacyText(legacy);

            team.prefix(legacySerializer.deserialize(parts[0]));
            team.suffix(legacySerializer.deserialize(parts[1]));

            obj.getScore(entryKey).setScore(score--);
            index++;
        }

        player.setScoreboard(board);
    }

    private String[] splitLegacyText(String legacy) {
        StringBuilder prefix = new StringBuilder();
        StringBuilder suffix = new StringBuilder();
        String lastColor = "";
        int visible = 0;

        for (int i = 0; i < legacy.length(); i++) {
            char c = legacy.charAt(i);
            if (c == '§' && i + 1 < legacy.length()) {
                lastColor = "§" + legacy.charAt(i + 1);
                if (visible < 16) prefix.append(lastColor);
                else if (visible < 32) suffix.append(lastColor);
                i++;
                continue;
            }

            if (visible < 16) prefix.append(c);
            else if (visible < 32) suffix.append(c);
            else break;

            visible++;
        }

        if (!lastColor.isEmpty() && !suffix.toString().startsWith("§")) {
            suffix.insert(0, lastColor);
        }

        return new String[]{prefix.toString(), suffix.toString()};
    }
}
