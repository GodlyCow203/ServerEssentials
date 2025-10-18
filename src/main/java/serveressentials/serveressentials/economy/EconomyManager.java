package serveressentials.serveressentials.economy;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class EconomyManager implements Economy {

    private static final Map<UUID, Double> balances = new HashMap<>();
    private static File file;
    private static FileConfiguration config;

    public static void loadBalances(File dataFolder) {
        File storageFolder = new File(dataFolder, "storage");
        if (!storageFolder.exists()) {
            storageFolder.mkdirs();
        }

        file = new File(storageFolder, "balances.yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
                Bukkit.getLogger().info("[EconomyManager] Created new storage/balances.yml file.");
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
            } catch (IllegalArgumentException ignored) {
                Bukkit.getLogger().warning("[EconomyManager] Invalid UUID in balances.yml: " + key);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double get(UUID uuid) {
        return balances.getOrDefault(uuid, 0.0);
    }

    private void set(UUID uuid, double amount) {
        balances.put(uuid, amount);
        saveBalances();
    }

    private void add(UUID uuid, double amount) {
        set(uuid, get(uuid) + amount);
    }

    private void take(UUID uuid, double amount) {
        set(uuid, Math.max(0, get(uuid) - amount));
    }


    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return "ServerEssentialsEconomy";
    }

    @Override
    public boolean hasBankSupport() {
        return false; // Not implemented
    }

    @Override
    public int fractionalDigits() {
        return 2;
    }

    @Override
    public String format(double amount) {
        return String.format("$%.2f", amount);
    }

    @Override
    public String currencyNamePlural() {
        return "Dollars";
    }

    @Override
    public String currencyNameSingular() {
        return "Dollar";
    }

    @Override
    public boolean hasAccount(String s) {
        return false;
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return true;
    }

    @Override
    public boolean hasAccount(String s, String s1) {
        return false;
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer, String s) {
        return false;
    }

    @Override
    public double getBalance(String s) {
        return 0;
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return get(player.getUniqueId());
    }

    @Override
    public double getBalance(String s, String s1) {
        return 0;
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer, String s) {
        return 0;
    }

    @Override
    public boolean has(String s, double v) {
        return false;
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return get(player.getUniqueId()) >= amount;
    }

    @Override
    public boolean has(String s, String s1, double v) {
        return false;
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, String s, double v) {
        return false;
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) return new EconomyResponse(0, get(player.getUniqueId()), EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative funds");
        if (!has(player, amount)) return new EconomyResponse(0, get(player.getUniqueId()), EconomyResponse.ResponseType.FAILURE, "Insufficient funds");

        take(player.getUniqueId(), amount);
        return new EconomyResponse(amount, get(player.getUniqueId()), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, String s1, double v) {
        return null;
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse depositPlayer(String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) return new EconomyResponse(0, get(player.getUniqueId()), EconomyResponse.ResponseType.FAILURE, "Cannot deposit negative funds");

        add(player.getUniqueId(), amount);
        return new EconomyResponse(amount, get(player.getUniqueId()), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse depositPlayer(String s, String s1, double v) {
        return null;
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, String s, double v) {
        return null;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        if (!balances.containsKey(player.getUniqueId())) {
            set(player.getUniqueId(), 0.0);
        }
        return true;
    }

    @Override
    public boolean createPlayerAccount(String s, String s1) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer, String s) {
        return false;
    }

    @Override public EconomyResponse createBank(String name, String player) { return notSupported(); }

    @Override
    public EconomyResponse createBank(String s, OfflinePlayer offlinePlayer) {
        return null;
    }

    @Override public EconomyResponse deleteBank(String name) { return notSupported(); }
    @Override public EconomyResponse bankBalance(String name) { return notSupported(); }
    @Override public EconomyResponse bankHas(String name, double amount) { return notSupported(); }
    @Override public EconomyResponse bankWithdraw(String name, double amount) { return notSupported(); }
    @Override public EconomyResponse bankDeposit(String name, double amount) { return notSupported(); }
    @Override public EconomyResponse isBankOwner(String name, String playerName) { return notSupported(); }

    @Override
    public EconomyResponse isBankOwner(String s, OfflinePlayer offlinePlayer) {
        return null;
    }

    @Override public EconomyResponse isBankMember(String name, String playerName) { return notSupported(); }

    @Override
    public EconomyResponse isBankMember(String s, OfflinePlayer offlinePlayer) {
        return null;
    }

    @Override
    public List<String> getBanks() {
        return List.of();
    }

    @Override
    public boolean createPlayerAccount(String s) {
        return false;
    }

    private EconomyResponse notSupported() {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banking not supported");
    }
}
