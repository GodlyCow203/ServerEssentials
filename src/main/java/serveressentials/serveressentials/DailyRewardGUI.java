package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DailyRewardGUI implements CommandExecutor {

    private static final HashMap<UUID, Double> balances = new HashMap<>();
    private static File file;
    private static FileConfiguration config;

    public static void loadBalances() {
        file = new File(Bukkit.getServer().getPluginManager().getPlugin("ServerEssentials").getDataFolder(), "balances.yml");

        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
                Bukkit.getLogger().info("Created new balances.yml file.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                double balance = config.getDouble(key);
                balances.put(uuid, balance);
                Bukkit.getLogger().info("Loaded balance for " + uuid + ": " + balance);
            } catch (IllegalArgumentException ignored) {
                Bukkit.getLogger().warning("Invalid UUID in balances.yml: " + key);
            }
        }
    }

    public static void saveBalances() {
        if (config == null || file == null) return;

        for (Map.Entry<UUID, Double> entry : balances.entrySet()) {
            config.set(entry.getKey().toString(), entry.getValue());
        }

        try {
            config.save(file);
            Bukkit.getLogger().info("Saved balances to balances.yml.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static double getBalance(@NotNull OfflinePlayer player) {
        return balances.getOrDefault(player.getUniqueId(), 0.0);
    }

    public static double getBalance(@NotNull UUID uuid) {
        return balances.getOrDefault(uuid, 0.0);
    }

    public static void setBalance(@NotNull UUID uuid, double amount) {
        balances.put(uuid, amount);
        saveBalances();
    }

    public static void setBalance(@NotNull Player player, double amount) {
        setBalance(player.getUniqueId(), amount);
    }

    public static void addBalance(@NotNull UUID uuid, double amount) {
        double current = getBalance(uuid);
        balances.put(uuid, current + amount);
        saveBalances();
    }

    public static void addBalance(@NotNull OfflinePlayer player, double amount) {
        addBalance(player.getUniqueId(), amount);
    }

    public static void takeBalance(@NotNull UUID uuid, double amount) {
        double current = getBalance(uuid);
        balances.put(uuid, Math.max(0, current - amount));
        saveBalances();
    }

    public static void takeBalance(@NotNull OfflinePlayer player, double amount) {
        takeBalance(player.getUniqueId(), amount);
    }

    public static boolean withdraw(@NotNull OfflinePlayer player, double amount) {
        double balance = getBalance(player);
        if (balance < amount) return false;

        setBalance(player.getUniqueId(), balance - amount);
        return true;
    }

    public static void deposit(@NotNull OfflinePlayer player, double amount) {
        addBalance(player, amount);
    }

    public static void resetBalance(@NotNull Player player) {
        setBalance(player.getUniqueId(), 0.0);
    }

    public static List<Map.Entry<UUID, Double>> getTopBalances(int limit) {
        List<Map.Entry<UUID, Double>> list = new ArrayList<>(balances.entrySet());
        list.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        return list.subList(0, Math.min(limit, list.size()));
    }

    public static void removeBalance(@NotNull UUID uniqueId, double price) {

    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return false;
    }
}
