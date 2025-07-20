package serveressentials.serveressentials;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleScoreboard {

    private final ServerEssentials plugin;
    private FileConfiguration scoreboardConfig;
    private File scoreboardFile;
    private static final Set<UUID> disabledPlayers = new HashSet<>();

    private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");

    public SimpleScoreboard(ServerEssentials plugin) {
        this.plugin = plugin;
        createScoreboardFile();
        startUpdater();
    }

    private void createScoreboardFile() {
        scoreboardFile = new File(plugin.getDataFolder(), "scoreboard.yml");

        if (!scoreboardFile.exists()) {
            scoreboardFile.getParentFile().mkdirs();
            try {
                if (scoreboardFile.createNewFile()) {
                    scoreboardConfig = YamlConfiguration.loadConfiguration(scoreboardFile);

                    scoreboardConfig.set("title", "<#FF7633> YourServer");
                    scoreboardConfig.set("lines", Arrays.asList(
                            "<#AAAAAA>━━━━━━━━━━━━━━━",
                            "<#FFF67A>%player_name%:",
                            "",
                            "",
                            "<#86FF44>| Balance:",
                            "  &f%se_money%",
                            "<#FFC7A3>| Ping: &f%player_ping%ms",
                            "<#9370DB>| Time Played:      &r%se_playtime%",
                            "<#8CA2FF>| Rank: &r%se_rank%",
                            "<#DC143C>| Deaths: &r%se_death%",
                            "<#00FF7F>| Kills: &r%se_kills%",
                            "",
                            "<#AAAAAA>━━━━━━━━━━━━━━━"
                    ));

                    scoreboardConfig.save(scoreboardFile);
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create scoreboard.yml: " + e.getMessage());
            }
        } else {
            scoreboardConfig = YamlConfiguration.loadConfiguration(scoreboardFile);
        }
    }

    public void reloadScoreboardConfig() {
        scoreboardConfig = YamlConfiguration.loadConfiguration(scoreboardFile);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isDisabled(player)) {
                setScoreboard(player);
            }
        }
    }

    public static boolean isDisabled(Player player) {
        return disabledPlayers.contains(player.getUniqueId());
    }

    public static void disable(Player player) {
        disabledPlayers.add(player.getUniqueId());
        removeScoreboard(player);
    }

    public static void enable(Player player) {
        disabledPlayers.remove(player.getUniqueId());
        setScoreboard(player);
    }

    public static void removeScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    private void startUpdater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!isDisabled(player)) {
                        setScoreboard(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Every second
    }

    public static void setScoreboard(Player player) {
        if (isDisabled(player)) return;

        ServerEssentials plugin = JavaPlugin.getPlugin(ServerEssentials.class);
        SimpleScoreboard instance = plugin.getScoreboard();

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;

        Scoreboard board = manager.getNewScoreboard();

        String rawTitle = instance.scoreboardConfig.getString("title", "<#FFD700>&lYourServer");
        String titleWithPlaceholders = PlaceholderAPI.setPlaceholders(player, rawTitle);
        String legacyTitle = translateColors(titleWithPlaceholders);

        Objective objective = board.registerNewObjective("main", "dummy", legacyTitle);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<String> rawLines = instance.scoreboardConfig.getStringList("lines");
        List<String> finalLines = new ArrayList<>();

        for (String rawLine : rawLines) {
            String processed = translateColors(PlaceholderAPI.setPlaceholders(player, rawLine));
            finalLines.addAll(splitLongLine(processed)); // Auto-split long lines
        }

        int score = finalLines.size();
        for (int i = 0; i < finalLines.size(); i++) {
            String line = finalLines.get(i);
            String entry = ChatColor.values()[i % ChatColor.values().length].toString();

            Team team = board.registerNewTeam("line" + i);
            team.addEntry(entry);

            if (line.length() <= 16) {
                team.setPrefix(line);
                team.setSuffix("");
            } else {
                String prefix = line.substring(0, 16);
                String suffix = ChatColor.getLastColors(prefix) + line.substring(16, Math.min(line.length(), 32));
                team.setPrefix(prefix);
                team.setSuffix(suffix);
            }

            objective.getScore(entry).setScore(score--);
        }

        player.setScoreboard(board);
    }

    private static String translateColors(String input) {
        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            String replacement = toLegacyHex(hex);
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);

        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    private static String toLegacyHex(String hex) {
        char[] chars = hex.toCharArray();
        return "§x" +
                "§" + chars[0] +
                "§" + chars[1] +
                "§" + chars[2] +
                "§" + chars[3] +
                "§" + chars[4] +
                "§" + chars[5];
    }

    private static List<String> splitLongLine(String line) {
        List<String> result = new ArrayList<>();
        int index = 0;
        while (index < line.length()) {
            int end = Math.min(index + 32, line.length());
            String part = line.substring(index, end);
            if (index != 0) {
                part = ChatColor.getLastColors(result.get(result.size() - 1)) + part;
            }
            result.add(part);
            index += 32;
        }
        return result;
    }
}
