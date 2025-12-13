package net.lunark.io.economy;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.lunark.io.database.DatabaseManager;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ShopDataManager {
    private final Plugin plugin;
    private final DatabaseManager dbManager;
    private static final String POOL_KEY = "shop";  // Changed from "main" to "shop"
    private static final String TABLE_SECTIONS = "shop_sections";
    private static final String TABLE_MAIN = "shop_main";

    public ShopDataManager(Plugin plugin, DatabaseManager dbManager) {
        this.plugin = plugin;
        this.dbManager = dbManager;
        initializeTables();
    }

    private void initializeTables() {
        dbManager.executeUpdate(POOL_KEY,
                "CREATE TABLE IF NOT EXISTS " + TABLE_SECTIONS + " (" +
                        "section_name TEXT PRIMARY KEY, " +
                        "title TEXT NOT NULL, " +
                        "size INTEGER NOT NULL, " +
                        "pages INTEGER NOT NULL DEFAULT 1, " +
                        "player_head_slot INTEGER NOT NULL DEFAULT -1, " +
                        "close_button_slot INTEGER NOT NULL DEFAULT -1, " +
                        "layout_json TEXT NOT NULL, " +
                        "items_json TEXT NOT NULL, " +
                        "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")"
        ).exceptionally(ex -> {
            plugin.getLogger().severe("Failed to create shop_sections table: " + ex.getMessage());
            return null;
        }).join();

        dbManager.executeUpdate(POOL_KEY,
                "CREATE TABLE IF NOT EXISTS " + TABLE_MAIN + " (" +
                        "config_key TEXT PRIMARY KEY, " +
                        "title TEXT NOT NULL, " +
                        "size INTEGER NOT NULL, " +
                        "layout_json TEXT NOT NULL, " +
                        "sections_json TEXT NOT NULL, " +
                        "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")"
        ).exceptionally(ex -> {
            plugin.getLogger().severe("Failed to create shop_main table: " + ex.getMessage());
            return null;
        }).join();
    }

    public CompletableFuture<Boolean> saveSectionConfig(String sectionName, ShopSectionConfig section) {
        String layoutJson = JsonHelper.toJson(section.layout);
        String itemsJson = JsonHelper.toJson(section.items);

        return dbManager.executeUpdate(POOL_KEY,
                        "INSERT OR REPLACE INTO " + TABLE_SECTIONS + " " +
                                "(section_name, title, size, pages, player_head_slot, close_button_slot, layout_json, items_json) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                        sectionName,
                        section.title != null ? section.title : "",
                        section.size,
                        section.pages,
                        section.playerHeadSlot,
                        section.closeButtonSlot,
                        layoutJson,
                        itemsJson
                ).thenApply(v -> true)
                .exceptionally(ex -> {
                    plugin.getLogger().severe("Failed to save section config: " + ex.getMessage());
                    return false;
                });
    }

    public CompletableFuture<Boolean> saveMainConfig(MainShopConfig main) {
        String layoutJson = JsonHelper.toJson(main.layout);
        String sectionsJson = JsonHelper.toJson(main.sectionButtons);

        return dbManager.executeUpdate(POOL_KEY,
                        "INSERT OR REPLACE INTO " + TABLE_MAIN + " " +
                                "(config_key, title, size, layout_json, sections_json) " +
                                "VALUES (?, ?, ?, ?, ?)",
                        "main",
                        main.title != null ? main.title : "",
                        main.size,
                        layoutJson,
                        sectionsJson
                ).thenApply(v -> true)
                .exceptionally(ex -> {
                    plugin.getLogger().severe("Failed to save main config: " + ex.getMessage());
                    return false;
                });
    }

    public CompletableFuture<ShopSectionConfig> loadSectionConfig(String sectionName) {
        return dbManager.executeQuery(POOL_KEY,
                "SELECT title, size, pages, player_head_slot, close_button_slot, layout_json, items_json " +
                        "FROM " + TABLE_SECTIONS + " WHERE section_name = ?",
                (ResultSet rs) -> {
                    if (!rs.next()) return null;
                    ShopSectionConfig section = new ShopSectionConfig();
                    section.title = rs.getString("title");
                    section.size = rs.getInt("size");
                    section.pages = rs.getInt("pages");
                    section.playerHeadSlot = rs.getInt("player_head_slot");
                    section.closeButtonSlot = rs.getInt("close_button_slot");

                    Type layoutType = new TypeToken<Map<Integer, ShopSectionConfig.LayoutItem>>(){}.getType();
                    section.layout = JsonHelper.fromJson(rs.getString("layout_json"), layoutType);

                    Type itemsType = new TypeToken<Map<String, ShopSectionConfig.ShopItem>>(){}.getType();
                    section.items = JsonHelper.fromJson(rs.getString("items_json"), itemsType);

                    return section;
                },
                sectionName
        ).thenApply(opt -> opt.orElse(null));
    }


    public CompletableFuture<MainShopConfig> loadMainConfig() {
        return dbManager.executeQuery(POOL_KEY,
                "SELECT title, size, layout_json, sections_json FROM " + TABLE_MAIN + " WHERE config_key = ?",
                (ResultSet rs) -> {
                    if (!rs.next()) return null;
                    MainShopConfig main = new MainShopConfig();
                    main.title = rs.getString("title");
                    main.size = rs.getInt("size");

                    Type layoutType = new TypeToken<Map<Integer, MainShopConfig.LayoutItem>>(){}.getType();
                    Type sectionsType = new TypeToken<Map<Integer, MainShopConfig.SectionButton>>(){}.getType();

                    main.layout = JsonHelper.fromJson(rs.getString("layout_json"), layoutType);
                    main.sectionButtons = JsonHelper.fromJson(rs.getString("sections_json"), sectionsType);
                    return main;
                },
                "main"
        ).thenApply(opt -> opt.orElse(null));
    }

    public CompletableFuture<Boolean> sectionExists(String sectionName) {
        return dbManager.executeQuery(POOL_KEY,
                "SELECT 1 FROM " + TABLE_SECTIONS + " WHERE section_name = ? LIMIT 1",
                rs -> rs.next() ? 1 : null,
                sectionName
        ).thenApply(opt -> opt.isPresent());
    }

    public CompletableFuture<Boolean> mainConfigExists() {
        return dbManager.executeQuery(POOL_KEY,
                "SELECT 1 FROM " + TABLE_MAIN + " WHERE config_key = ? LIMIT 1",
                rs -> rs.next() ? 1 : null,
                "main"
        ).thenApply(opt -> opt.isPresent());
    }

    public static class JsonHelper {
        private static final Gson gson = new Gson();

        public static String toJson(Object obj) {
            return gson.toJson(obj);
        }

        public static <T> T fromJson(String json, Class<T> clazz) {
            if (json == null || json.isEmpty()) {
                try {
                    return clazz.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    return null;
                }
            }
            return gson.fromJson(json, clazz);
        }

        public static <T> T fromJson(String json, java.lang.reflect.Type type) {
            if (json == null || json.isEmpty()) {
                return null;
            }
            return gson.fromJson(json, type);
        }
    }
}