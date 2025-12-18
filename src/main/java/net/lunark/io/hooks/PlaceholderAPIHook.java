/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  me.clip.placeholderapi.PlaceholderAPI
 *  org.bukkit.Bukkit
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 */
package net.lunark.io.hooks;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PlaceholderAPIHook {
    private static PlaceholderAPIHook instance;
    private boolean available = false;
    private Plugin placeholderAPIPlugin = null;

    private PlaceholderAPIHook() {
    }

    public static PlaceholderAPIHook getInstance() {
        if (instance == null) {
            instance = new PlaceholderAPIHook();
        }
        return instance;
    }

    public boolean init() {
        Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        if (papi == null || !papi.isEnabled()) {
            this.cleanup();
            return false;
        }
        this.placeholderAPIPlugin = papi;
        this.available = true;
        return true;
    }

    public boolean isAvailable() {
        return this.available;
    }

    public String setPlaceholders(Player player, String text) {
        if (!this.available || text == null) {
            return text;
        }
        try {
            return PlaceholderAPI.setPlaceholders((Player)player, (String)text);
        } catch (Exception e) {
            return text;
        }
    }

    public void cleanup() {
        this.placeholderAPIPlugin = null;
        this.available = false;
    }
}

