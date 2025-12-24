package net.godlycow.org.nick;

import net.godlycow.org.nick.storage.NickStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class NickManager {
    private final MiniMessage mini = MiniMessage.miniMessage();
    private final Map<UUID, String> nickCache = new HashMap<>();
    private final NickStorage storage;
    private final Plugin plugin;

    public NickManager(Plugin plugin, NickStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
        loadOnlinePlayers();
    }

    private void loadOnlinePlayers() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            storage.getNickname(player.getUniqueId()).thenAccept(opt -> {
                if (opt.isPresent()) {
                    applyNick(player.getUniqueId(), opt.get());
                    plugin.getLogger().info("Loaded nickname for " + player.getName() + ": " + opt.get());
                }
            }).exceptionally(ex -> {
                plugin.getLogger().warning("Failed to load nickname for " + player.getName() + ": " + ex.getMessage());
                return null;
            });
        });
    }

    public void applyNick(UUID playerId, String nick) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) return;

        try {
            Component displayName = mini.deserialize(nick);
            player.displayName(displayName);
            player.playerListName(displayName);
            nickCache.put(playerId, nick);
            plugin.getLogger().fine("Applied nickname to " + player.getName() + ": " + nick);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to apply nickname '" + nick + "' to " + player.getName() + ": " + e.getMessage());
        }
    }

    public void removeNick(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            Component originalName = Component.text(player.getName());
            player.displayName(originalName);
            player.playerListName(originalName);
            plugin.getLogger().fine("Removed nickname from " + player.getName());
        }
        nickCache.remove(playerId);
        storage.removeNickname(playerId);
    }

    public void reloadAllNicks() {
        nickCache.clear();
        loadOnlinePlayers();
        plugin.getLogger().info("Reloaded all nicknames");
    }

    public Map<UUID, String> getAllCachedNicks() {
        return new HashMap<>(nickCache);
    }
}