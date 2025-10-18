package serveressentials.serveressentials.staff;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import serveressentials.serveressentials.ServerEssentials;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BanManager {

    private static ServerEssentials plugin;
    private static File banFile;
    private static FileConfiguration banConfig;

    private static File configFile;
    private static FileConfiguration config;

    public BanManager(ServerEssentials pl) {
        plugin = pl;
        setupBanFile();
        setupConfigFile();
    }

    private static void setupBanFile() {
        banFile = new File(plugin.getDataFolder(), "storage/bans.yml");
        if (!banFile.exists()) {
            banFile.getParentFile().mkdirs();
            try {
                banFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        banConfig = YamlConfiguration.loadConfiguration(banFile);
    }

    private static void setupConfigFile() {
        configFile = new File(plugin.getDataFolder(), "config/bans/bans.yml");

        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        boolean changed = false;
        if (!config.contains("server-name")) {
            config.set("server-name", "MyCoolServer");
            changed = true;
        }
        if (!config.contains("discord-link")) {
            config.set("discord-link", "https://discord.gg/example");
            changed = true;
        }
        if (changed) saveConfig();
    }

    // ✅ Called from ReloadManager
    public static void reload() {
        // Make sure files exist before reloading
        if (!banFile.exists() || !configFile.exists()) {
            setupBanFile();
            setupConfigFile();
        }

        banConfig = YamlConfiguration.loadConfiguration(banFile);
        config = YamlConfiguration.loadConfiguration(configFile);

        Bukkit.getLogger().info("[ServerEssentials] BanManager configs reloaded.");
    }

    public void banPlayer(UUID uuid, String name, String reason, String bannedBy, long until) {
        String path = "BannedPlayers." + uuid;
        banConfig.set(path + ".name", name);
        banConfig.set(path + ".reason", reason);
        banConfig.set(path + ".bannedBy", bannedBy);
        banConfig.set(path + ".server", config.getString("server-name"));
        banConfig.set(path + ".discord", config.getString("discord-link"));
        banConfig.set(path + ".bannedUntil", until);
        saveBans();
    }

    public boolean isBanned(UUID uuid) {
        String path = "BannedPlayers." + uuid;
        if (!banConfig.contains(path)) return false;

        long until = banConfig.getLong(path + ".bannedUntil");
        if (until == -1) return true;

        if (System.currentTimeMillis() > until) {
            unbanPlayer(uuid); // Expired ban
            return false;
        }
        return true;
    }

    public void unbanPlayer(UUID uuid) {
        banConfig.set("BannedPlayers." + uuid, null);
        saveBans();
    }

    public String getBanMessage(UUID uuid) {
        String path = "BannedPlayers." + uuid;
        if (!banConfig.contains(path)) return "§cYou are banned from this server.";

        String reason = banConfig.getString(path + ".reason", "No reason specified.");
        String bannedBy = banConfig.getString(path + ".bannedBy", "Unknown");
        String server = config.getString("server-name", "Server");
        String discord = config.getString("discord-link", "N/A");
        long until = banConfig.getLong(path + ".bannedUntil");

        String timeLeft = (until == -1) ? "Permanent" : new Date(until).toString();

        return "§cYou are banned from §4" + server + "\n" +
                "§7Banned by: §f" + bannedBy + "\n" +
                "§7Reason: §f" + reason + "\n" +
                "§7Until: §f" + timeLeft + "\n\n" +
                "§7Appeal at: §9" + discord;
    }

    public UUID getUUIDFromName(String name) {
        if (banConfig.contains("BannedPlayers")) {
            for (String key : banConfig.getConfigurationSection("BannedPlayers").getKeys(false)) {
                String storedName = banConfig.getString("BannedPlayers." + key + ".name");
                if (storedName != null && storedName.equalsIgnoreCase(name)) {
                    try {
                        return UUID.fromString(key);
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        }
        return null;
    }

    public Set<String> getAllBannedUUIDs() {
        if (banConfig.contains("BannedPlayers")) {
            return banConfig.getConfigurationSection("BannedPlayers").getKeys(false);
        }
        return Collections.emptySet();
    }

    public String getNameFromUUID(String uuid) {
        return banConfig.getString("BannedPlayers." + uuid + ".name", "Unknown");
    }

    public String getReason(String uuid) {
        return banConfig.getString("BannedPlayers." + uuid + ".reason", "No reason specified");
    }

    public long getUntil(String uuid) {
        return banConfig.getLong("BannedPlayers." + uuid + ".bannedUntil", -1);
    }

    private static void saveBans() {
        try {
            banConfig.save(banFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
