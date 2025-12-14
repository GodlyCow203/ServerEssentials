package net.lunark.io.hooks;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PlaceholderAPIHook {
    private static PlaceholderAPIHook instance;
    private boolean available = false;
    private Plugin placeholderAPIPlugin = null;

    private PlaceholderAPIHook() {}

    public static PlaceholderAPIHook getInstance() {
        if (instance == null) {
            instance = new PlaceholderAPIHook();
        }
        return instance;
    }

    public boolean init() {
        Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        if (papi == null || !papi.isEnabled()) {
            return false;
        }

        placeholderAPIPlugin = papi;
        available = true;
        return true;
    }

    public boolean isAvailable() {
        return available;
    }

    public String setPlaceholders(Player player, String text) {
        if (!available || text == null) {
            return text;
        }
        return PlaceholderAPI.setPlaceholders(player, text);
    }

    public boolean setPlaceholdersAvailable() {
        return available;
    }

    public void cleanup() {
        placeholderAPIPlugin = null;
        available = false;
    }
}