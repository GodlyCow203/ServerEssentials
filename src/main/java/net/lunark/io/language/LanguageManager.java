package net.lunark.io.language;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;

public class LanguageManager {
    private final Plugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Gson gson = new Gson();

    private final Map<String, Map<String, JsonElement>> languageData = new HashMap<>();
    private String defaultLanguage = "en";

    public LanguageManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public LanguageManager() {
        plugin = null;
    }

    public void loadLanguages() {
        languageData.clear();

        File langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists()) {
            langDir.mkdirs();
        }

        File defaultFile = new File(langDir, "en.json");
        if (!defaultFile.exists()) {
            plugin.saveResource("lang/en.json", false);
        }

        File[] files = langDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) return;

        for (File file : files) {
            try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
                Type type = new TypeToken<Map<String, JsonElement>>(){}.getType();
                Map<String, JsonElement> data = gson.fromJson(reader, type);

                String langId = file.getName().replace(".json", "").toLowerCase(Locale.ROOT);
                languageData.put(langId, data != null ? data : new HashMap<>());

                plugin.getLogger().info("Loaded language: " + langId + " (" + data.size() + " keys)");
            } catch (Exception ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load language: " + file.getName(), ex);
            }
        }
    }



    public Set<String> getAvailableLanguages() {
        return Collections.unmodifiableSet(languageData.keySet());
    }

    public boolean hasLanguage(String langId) {
        return languageData.containsKey(langId.toLowerCase(Locale.ROOT));
    }

    public void setDefaultLanguage(String langId) {
        if (hasLanguage(langId)) {
            this.defaultLanguage = langId.toLowerCase(Locale.ROOT);
        }
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    private Map<String, JsonElement> getLangMap(String langId) {
        langId = langId != null ? langId.toLowerCase(Locale.ROOT) : defaultLanguage;
        return languageData.getOrDefault(langId, languageData.getOrDefault(defaultLanguage, Map.of()));
    }

    private Optional<JsonElement> getRawElement(String langId, String key) {
        Map<String, JsonElement> langMap = getLangMap(langId);
        String[] parts = key.split("\\.");
        JsonElement element = null;

        for (String part : parts) {
            if (element == null) {
                element = langMap.get(part);
            } else if (element.isJsonObject()) {
                element = element.getAsJsonObject().get(part);
            } else {
                return Optional.empty();
            }

            if (element == null) return Optional.empty();
        }

        return Optional.ofNullable(element);
    }

    public String getString(String langId, String key, String def, ComponentPlaceholder... placeholders) {
        JsonElement element = getRawElement(langId, key).orElse(null);

        String raw;
        if (element == null) {
            raw = def;
        } else if (element.isJsonPrimitive()) {
            raw = element.getAsString();
        } else if (element.isJsonArray()) {
            List<String> lines = new ArrayList<>();
            element.getAsJsonArray().forEach(el -> lines.add(el.getAsString()));
            raw = String.join("\n", lines);
        } else {
            raw = gson.toJson(element);
        }

        return replacePlaceholders(raw, placeholders);
    }

    public String getString(String key, String def, ComponentPlaceholder... placeholders) {
        return getString(null, key, def, placeholders);
    }

    public Component getComponent(String langId, String key, String def, ComponentPlaceholder... placeholders) {
        String raw = getString(langId, key, def, placeholders);
        try {
            return miniMessage.deserialize(raw);
        } catch (Exception ex) {
            plugin.getLogger().log(Level.WARNING, "Invalid MiniMessage at '" + key + "': " + raw, ex);
            return Component.text(raw);
        }
    }

    public Component getComponent(String key, String def, ComponentPlaceholder... placeholders) {
        return getComponent(null, key, def, placeholders);
    }

    public List<Component> getComponentList(String langId, String key) {
        JsonElement element = getRawElement(langId, key).orElse(null);
        List<Component> components = new ArrayList<>();

        if (element == null) return components;

        if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(el -> {
                if (el.isJsonPrimitive()) {
                    components.add(miniMessage.deserialize(el.getAsString()));
                }
            });
        } else if (element.isJsonPrimitive()) {
            components.add(miniMessage.deserialize(element.getAsString()));
        }

        return components;
    }

    public void reloadLanguages() {
        plugin.getLogger().info("Reloading language files...");
        loadLanguages(); // Reuse existing load logic
        plugin.getLogger().info("Reloaded " + languageData.size() + " languages!");
    }

    private String replacePlaceholders(String raw, ComponentPlaceholder... placeholders) {
        if (placeholders == null) return raw;

        for (ComponentPlaceholder ph : placeholders) {
            raw = raw.replace(ph.placeholder(), ph.value());
        }
        return raw;
    }

    public record ComponentPlaceholder(String placeholder, String value) {
        public static ComponentPlaceholder of(String placeholder, Object value) {
            return new ComponentPlaceholder(placeholder, String.valueOf(value));
        }
    }
}