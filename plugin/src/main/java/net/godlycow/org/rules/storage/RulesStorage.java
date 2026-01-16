package net.godlycow.org.rules.storage;

import net.godlycow.org.database.DatabaseManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RulesStorage {
    private final DatabaseManager dbManager;
    private final Plugin plugin;
    private final String poolKey = "rules";

    public RulesStorage(Plugin plugin, DatabaseManager dbManager) {
        this.plugin = plugin;
        this.dbManager = dbManager;
        initTables();
    }

    private void initTables() {
        String createRulesTable = "CREATE TABLE IF NOT EXISTS rules (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "rule_text TEXT NOT NULL, " +
                "version INTEGER NOT NULL, " +
                "order_index INTEGER NOT NULL, " +
                "slot INTEGER NOT NULL DEFAULT -1)";

        String createAcceptanceTable = "CREATE TABLE IF NOT EXISTS player_acceptances (" +
                "player_uuid TEXT PRIMARY KEY, " +
                "accepted_version INTEGER NOT NULL, " +
                "accepted_timestamp BIGINT NOT NULL)";

        CompletableFuture.allOf(
                dbManager.executeUpdate(poolKey, createRulesTable),
                dbManager.executeUpdate(poolKey, createAcceptanceTable)
        ).join();

        loadInitialRulesFromYaml();
    }

    private void loadInitialRulesFromYaml() {
        File rulesFile = new File(plugin.getDataFolder(), "rules.yml");
        if (!rulesFile.exists()) {
            plugin.saveResource("rules.yml", false);
        }

        String checkSql = "SELECT COUNT(*) as count FROM rules";
        dbManager.executeQuery(poolKey, checkSql, rs -> {
            if (rs.next() && rs.getInt("count") == 0) {
                plugin.getLogger().info("Rules table is empty. Loading from rules.yml...");

                FileConfiguration config = YamlConfiguration.loadConfiguration(rulesFile);
                List<?> rulesList = config.getList("rules");

                if (rulesList == null || rulesList.isEmpty()) {
                    plugin.getLogger().warning("No rules found in rules.yml!");
                    return 0;
                }

                String insertSql = "INSERT INTO rules (rule_text, version, order_index, slot) VALUES (?, ?, ?, ?)";
                List<CompletableFuture<Void>> inserts = new ArrayList<>();

                for (int i = 0; i < rulesList.size(); i++) {
                    Object ruleObj = rulesList.get(i);
                    String ruleText = null;
                    int slot = -1;

                    if (ruleObj instanceof String) {
                        ruleText = (String) ruleObj;
                        slot = 10 + (i * 2);
                    } else if (ruleObj instanceof org.bukkit.configuration.ConfigurationSection) {
                        org.bukkit.configuration.ConfigurationSection ruleConfig = (org.bukkit.configuration.ConfigurationSection) ruleObj;
                        ruleText = ruleConfig.getString("text");
                        slot = ruleConfig.getInt("slot", 10 + (i * 2));
                    } else if (ruleObj instanceof java.util.Map) {
                        java.util.Map<?, ?> ruleMap = (java.util.Map<?, ?>) ruleObj;
                        ruleText = String.valueOf(ruleMap.get("text"));
                        Object slotObj = ruleMap.get("slot");
                        slot = slotObj != null ? Integer.parseInt(String.valueOf(slotObj)) : 10 + (i * 2);
                    }

                    if (ruleText != null && !ruleText.trim().isEmpty()) {
                        inserts.add(dbManager.executeUpdate(poolKey, insertSql, ruleText, 1, i, slot));
                    }
                }

                CompletableFuture.allOf(inserts.toArray(new CompletableFuture[0])).join();
                plugin.getLogger().info("Loaded " + inserts.size() + " rules from rules.yml");
            }
            return 0;
        });
    }

    public CompletableFuture<List<Rule>> getAllRules() {
        String sql = "SELECT id, rule_text, version, order_index, slot FROM rules ORDER BY order_index ASC";
        return dbManager.executeQuery(poolKey, sql, rs -> {
            List<Rule> rules = new ArrayList<>();
            while (rs.next()) {
                rules.add(new Rule(
                        rs.getInt("id"),
                        rs.getString("rule_text"),
                        rs.getInt("version"),
                        rs.getInt("order_index"),
                        rs.getInt("slot")
                ));
            }
            return rules;
        }).thenApply(opt -> opt.orElseGet(ArrayList::new));
    }

    public CompletableFuture<Integer> getLatestVersion() {
        String sql = "SELECT MAX(version) as max_version FROM rules";
        return dbManager.executeQuery(poolKey, sql, rs ->
                rs.next() ? rs.getInt("max_version") : 0
        ).thenApply(opt -> opt.orElse(0));
    }

    public CompletableFuture<Boolean> hasAcceptedRules(UUID playerId) {
        return getLatestVersion().thenCompose(latestVersion -> {
            if (latestVersion == 0) return CompletableFuture.completedFuture(false);

            String sql = "SELECT accepted_version FROM player_acceptances WHERE player_uuid = ?";
            return dbManager.executeQuery(poolKey, sql,
                    rs -> rs.next() ? rs.getInt("accepted_version") : -1,
                    playerId.toString()
            ).thenApply(opt -> opt.filter(v -> v == latestVersion).isPresent());
        });
    }

    public CompletableFuture<Void> acceptRules(UUID playerId, int version) {
        String sql = "INSERT OR REPLACE INTO player_acceptances VALUES (?, ?, ?)";
        return dbManager.executeUpdate(poolKey, sql,
                playerId.toString(), version, System.currentTimeMillis());
    }

    public CompletableFuture<Void> reloadRulesFromConfig() {
        return getLatestVersion().thenCompose(currentVersion -> {
            File rulesFile = new File(plugin.getDataFolder(), "rules.yml");
            if (!rulesFile.exists()) {
                plugin.saveResource("rules.yml", false);
            }

            FileConfiguration config = YamlConfiguration.loadConfiguration(rulesFile);
            List<?> rulesList = config.getList("rules");

            if (rulesList == null || rulesList.isEmpty()) {
                plugin.getLogger().warning("No rules found in rules.yml during reload!");
                return CompletableFuture.completedFuture(null);
            }

            int newVersion = currentVersion + 1;
            String clearSql = "DELETE FROM rules";

            return dbManager.executeUpdate(poolKey, clearSql)
                    .thenCompose(v -> {
                        String insertSql = "INSERT INTO rules (rule_text, version, order_index, slot) VALUES (?, ?, ?, ?)";
                        List<CompletableFuture<Void>> inserts = new ArrayList<>();

                        for (int i = 0; i < rulesList.size(); i++) {
                            Object ruleObj = rulesList.get(i);
                            String ruleText = null;
                            int slot = -1;

                            if (ruleObj instanceof String) {
                                ruleText = (String) ruleObj;
                                slot = 10 + (i * 2);
                            } else if (ruleObj instanceof org.bukkit.configuration.ConfigurationSection) {
                                org.bukkit.configuration.ConfigurationSection ruleConfig = (org.bukkit.configuration.ConfigurationSection) ruleObj;
                                ruleText = ruleConfig.getString("text");
                                slot = ruleConfig.getInt("slot", 10 + (i * 2));
                            } else if (ruleObj instanceof java.util.Map) {
                                java.util.Map<?, ?> ruleMap = (java.util.Map<?, ?>) ruleObj;
                                ruleText = String.valueOf(ruleMap.get("text"));
                                Object slotObj = ruleMap.get("slot");
                                slot = slotObj != null ? Integer.parseInt(String.valueOf(slotObj)) : 10 + (i * 2);
                            }

                            if (ruleText != null && !ruleText.trim().isEmpty()) {
                                inserts.add(dbManager.executeUpdate(poolKey, insertSql, ruleText, newVersion, i, slot));
                            }
                        }

                        return CompletableFuture.allOf(inserts.toArray(new CompletableFuture[0]))
                                .thenRun(() -> {
                                    plugin.getLogger().info("Reloaded " + inserts.size() + " rules with version " + newVersion);
                                    dbManager.executeUpdate(poolKey, "DELETE FROM player_acceptances");
                                });
                    });
        });
    }
    public record Rule(int id, String text, int version, int orderIndex, int slot) {}
}