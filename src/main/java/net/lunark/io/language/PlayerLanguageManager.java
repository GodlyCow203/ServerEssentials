package net.lunark.io.language;

import net.lunark.io.language.storage.PlayerLanguageStorage;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerLanguageManager extends LanguageManager {
    private final PlayerLanguageStorage storage;
    private final LanguageManager languageManager;
    private final ConcurrentHashMap<UUID, String> cache = new ConcurrentHashMap<>();

    public PlayerLanguageManager(PlayerLanguageStorage storage, LanguageManager languageManager) {
        this.storage = storage;
        this.languageManager = languageManager;
    }

    public void setPlayerLanguage(UUID playerId, String languageCode) {
        if (!languageManager.hasLanguage(languageCode)) {
            throw new IllegalArgumentException("Invalid language: " + languageCode);
        }

        cache.put(playerId, languageCode);
        storage.saveLanguage(playerId, languageCode);
    }

    public String getPlayerLanguage(UUID playerId) {
        return cache.computeIfAbsent(playerId, storage::getLanguageSync);
    }

    public Component getMessageFor(Player player, String key, String def, LanguageManager.ComponentPlaceholder... placeholders) {
        String langId = player != null ? getPlayerLanguage(player.getUniqueId()) : languageManager.getDefaultLanguage();
        return languageManager.getComponent(langId, key, def, placeholders);
    }

    public List<Component> getMessageList(Player player, String key) {
        String langId = player != null ? getPlayerLanguage(player.getUniqueId()) : languageManager.getDefaultLanguage();
        return languageManager.getComponentList(langId, key);
    }

    public void loadPlayerLanguageAsync(UUID playerId) {
        storage.loadLanguage(playerId).thenAccept(lang -> {
            if (lang != null) {
                cache.put(playerId, lang);
            }
        });
    }
}