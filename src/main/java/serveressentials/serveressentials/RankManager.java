package serveressentials.serveressentials;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import net.md_5.bungee.api.ChatColor; // Bungee ChatColor for hex
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RankManager {

    private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");
    private final Map<String, Rank> ranks = new HashMap<>();
    private final Map<UUID, Rank> playerRanks = new HashMap<>();
    private final File rankFile;
    private final FileConfiguration rankConfig;
    private final File playerRanksFile;
    private final FileConfiguration playerRanksConfig;

    private final JavaPlugin plugin;

    public RankManager(JavaPlugin plugin) {
        this.plugin = plugin;

        this.rankFile = new File(plugin.getDataFolder(), "ranks.yml");
        if (!rankFile.exists()) {
            plugin.saveResource("ranks.yml", false);
        }
        this.rankConfig = YamlConfiguration.loadConfiguration(rankFile);

        this.playerRanksFile = new File(plugin.getDataFolder(), "playerranks.yml");
        if (!playerRanksFile.exists()) {
            try {
                playerRanksFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.playerRanksConfig = YamlConfiguration.loadConfiguration(playerRanksFile);

        loadRanksFromConfig();
        loadPlayerRanks();
    }

    public static String formatColors(String input) {
        if (input == null) return "";

        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hexCode = matcher.group(1);
            ChatColor hexColor = ChatColor.of("#" + hexCode);
            matcher.appendReplacement(buffer, hexColor.toString());
        }
        matcher.appendTail(buffer);

        return org.bukkit.ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    public void reloadConfig() {
        try {
            rankConfig.load(rankFile);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to reload ranks.yml");
            e.printStackTrace();
        }

        try {
            playerRanksConfig.load(playerRanksFile);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to reload playerranks.yml");
            e.printStackTrace();
        }

        loadRanksFromConfig();
        loadPlayerRanks();
    }

    public FileConfiguration getRankConfig() {
        return rankConfig;
    }

    public void loadRanksFromConfig() {
        ranks.clear();
        if (!rankConfig.isConfigurationSection("ranks")) return;

        for (String rankKey : rankConfig.getConfigurationSection("ranks").getKeys(false)) {
            String path = "ranks." + rankKey;
            String displayName = rankConfig.getString(path + ".display", rankKey.toUpperCase());
            String colorName = rankConfig.getString(path + ".color", "WHITE").toUpperCase();
            String prefix = rankConfig.getString(path + ".prefix", "");

            ChatColor color;
            try {
                color = ChatColor.valueOf(colorName);
            } catch (IllegalArgumentException e) {
                color = ChatColor.WHITE;
                plugin.getLogger().warning("Invalid color '" + colorName + "' for rank '" + rankKey + "'. Using WHITE.");
            }

            String group = rankConfig.getString(path + ".group", rankKey.toLowerCase());
            int weight = rankConfig.getInt(path + ".weight", 999);

            Rank rank = new Rank(rankKey, displayName, color, group, weight, prefix);
            ranks.put(rankKey.toLowerCase(), rank);
        }

        plugin.getLogger().info("Loaded " + ranks.size() + " ranks.");
    }

    public void loadPlayerRanks() {
        playerRanks.clear();
        for (String key : playerRanksConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                String rankKey = playerRanksConfig.getString(key).toLowerCase();
                Rank rank = ranks.get(rankKey);
                if (rank != null) {
                    playerRanks.put(uuid, rank);
                }
            } catch (IllegalArgumentException ignored) {}
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            Rank rank = getRank(player);
            updateTabName(player, rank);
        }
    }

    public Rank getRank(Player player) {
        return playerRanks.get(player.getUniqueId());
    }

    public Rank getRank(String rankKey) {
        if (rankKey == null) return null;
        return ranks.get(rankKey.toLowerCase());
    }

    public Collection<Rank> getAllRanks() {
        return ranks.values();
    }

    public void setRank(Player player, Rank rank) {
        if (rank == null) {
            removeRank(player);
            return;
        }

        playerRanks.put(player.getUniqueId(), rank);
        updateTabName(player, rank);
        savePlayerRanks();

        LuckPerms lp = LuckPermsProvider.get();
        User user = lp.getUserManager().getUser(player.getUniqueId());
        if (user != null) {
            for (Rank r : ranks.values()) {
                InheritanceNode node = InheritanceNode.builder(r.getPermissionGroup()).build();
                user.data().remove(node);
            }

            InheritanceNode newNode = InheritanceNode.builder(rank.getPermissionGroup()).build();
            user.data().add(newNode);
            lp.getUserManager().saveUser(user);
        }
    }

    public void removeRank(Player player) {
        playerRanks.remove(player.getUniqueId());
        player.setPlayerListName(player.getName());
        playerRanksConfig.set(player.getUniqueId().toString(), null);
        savePlayerRanks();

        LuckPerms lp = LuckPermsProvider.get();
        User user = lp.getUserManager().getUser(player.getUniqueId());
        if (user != null) {
            for (Rank r : ranks.values()) {
                InheritanceNode node = InheritanceNode.builder(r.getPermissionGroup()).build();
                user.data().remove(node);
            }
            lp.getUserManager().saveUser(user);
        }
    }

    public void updateTabName(Player player, Rank rank) {
        String name = player.getName();
        String prefix = "";

        if (rank != null) {
            prefix = formatColors(rank.getPrefix());
        }

        String displayName = prefix + name;
        player.setPlayerListName(displayName.length() > 32 ? displayName.substring(0, 32) : displayName);
    }

    public void savePlayerRanks() {
        for (UUID uuid : playerRanks.keySet()) {
            playerRanksConfig.set(uuid.toString(), playerRanks.get(uuid).getName());
        }
        try {
            playerRanksConfig.save(playerRanksFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
