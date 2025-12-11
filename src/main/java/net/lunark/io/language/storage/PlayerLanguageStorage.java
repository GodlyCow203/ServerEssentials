package net.lunark.io.language.storage;

import net.lunark.io.database.DatabaseManager;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public interface PlayerLanguageStorage {
    CompletableFuture<Void> saveLanguage(UUID playerId, String languageCode);
    CompletableFuture<String> loadLanguage(UUID playerId);
    String getLanguageSync(UUID playerId);

    class YamlStorage implements PlayerLanguageStorage {
        private final Plugin plugin;
        private final File file;
        private final Map<UUID, String> cache = new HashMap<>();

        public YamlStorage(Plugin plugin) {
            this.plugin = plugin;
            this.file = new File(plugin.getDataFolder(), "player_languages.yml");
            loadCache();
        }

        private void loadCache() {
            if (!file.exists()) return;

            try {
                Map<String, String> data = new HashMap<>();
                org.bukkit.configuration.file.YamlConfiguration yaml =
                        org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);

                for (String key : yaml.getKeys(false)) {
                    try {
                        UUID uuid = UUID.fromString(key);
                        String lang = yaml.getString(key);
                        if (lang != null) {
                            cache.put(uuid, lang);
                        }
                    } catch (IllegalArgumentException ex) {
                        plugin.getLogger().warning("Invalid UUID in language file: " + key);
                    }
                }
            } catch (Exception ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load player languages from YAML", ex);
            }
        }

        @Override
        public CompletableFuture<Void> saveLanguage(UUID playerId, String languageCode) {
            return CompletableFuture.runAsync(() -> {
                cache.put(playerId, languageCode);

                org.bukkit.configuration.file.YamlConfiguration yaml = new org.bukkit.configuration.file.YamlConfiguration();
                cache.forEach((uuid, lang) -> yaml.set(uuid.toString(), lang));

                try {
                    yaml.save(file);
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to save player language", e);
                }
            });
        }

        @Override
        public CompletableFuture<String> loadLanguage(UUID playerId) {
            return CompletableFuture.supplyAsync(() -> getLanguageSync(playerId));
        }

        @Override
        public String getLanguageSync(UUID playerId) {
            return cache.getOrDefault(playerId, "en");
        }
    }

    class SqlStorage implements PlayerLanguageStorage {
        private final DatabaseManager dbManager;
        private final String poolKey;
        private final Map<UUID, String> cache = new HashMap<>();

        public SqlStorage(DatabaseManager dbManager, String poolKey) {
            this.dbManager = dbManager;
            this.poolKey = poolKey;
            initTable();
        }

        private void initTable() {
            String sql = dbManager.getPoolKeys().stream()
                    .filter(key -> key.toLowerCase().contains("mysql"))
                    .findFirst()
                    .map(key -> "CREATE TABLE IF NOT EXISTS player_languages (" +
                            "player_uuid VARCHAR(36) PRIMARY KEY, " +
                            "language_code VARCHAR(10), " +
                            "updated_at BIGINT)")
                    .orElse("CREATE TABLE IF NOT EXISTS player_languages (" +
                            "player_uuid TEXT PRIMARY KEY, " +
                            "language_code TEXT, " +
                            "updated_at INTEGER)");

            dbManager.executeUpdate(poolKey, sql).exceptionally(ex -> {
                ex.printStackTrace();
                return null;
            });
        }

        @Override
        public CompletableFuture<Void> saveLanguage(UUID playerId, String languageCode) {
            cache.put(playerId, languageCode);

            String sql = dbManager.getPoolKeys().stream()
                    .filter(key -> key.toLowerCase().contains("mysql"))
                    .findFirst()
                    .map(key -> "INSERT INTO player_languages (player_uuid, language_code, updated_at) " +
                            "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE " +
                            "language_code=VALUES(language_code), updated_at=VALUES(updated_at)")
                    .orElse("INSERT OR REPLACE INTO player_languages VALUES (?, ?, ?)");

            return dbManager.executeUpdate(poolKey, sql,
                    playerId.toString(), languageCode, System.currentTimeMillis());
        }

        @Override
        public CompletableFuture<String> loadLanguage(UUID playerId) {
            return dbManager.executeQuery(poolKey,
                    "SELECT language_code FROM player_languages WHERE player_uuid = ?",
                    rs -> rs.next() ? rs.getString("language_code") : null,
                    playerId.toString()).thenApply(opt -> opt.orElse("en"));
        }

        @Override
        public String getLanguageSync(UUID playerId) {
            return cache.getOrDefault(playerId, "en");
        }
    }
}