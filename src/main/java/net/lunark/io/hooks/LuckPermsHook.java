/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  net.luckperms.api.LuckPerms
 *  net.luckperms.api.LuckPermsProvider
 *  org.bukkit.Bukkit
 *  org.bukkit.plugin.RegisteredServiceProvider
 */
package net.lunark.io.hooks;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class LuckPermsHook {
    private static LuckPermsHook instance;
    private LuckPerms api = null;
    private boolean available = false;

    private LuckPermsHook() {
    }

    public static LuckPermsHook getInstance() {
        if (instance == null) {
            instance = new LuckPermsHook();
        }
        return instance;
    }

    public boolean init() {
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null) {
            return false;
        }
        try {
            RegisteredServiceProvider provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
            if (provider != null) {
                this.api = (LuckPerms)provider.getProvider();
            }
        } catch (Exception e) {
            try {
                this.api = LuckPermsProvider.get();
            } catch (Exception ex) {
                this.api = null;
            }
        }
        this.available = this.api != null;
        return this.available;
    }

    public boolean isAvailable() {
        return this.available;
    }

    public LuckPerms getAPI() {
        return this.api;
    }

    public void cleanup() {
        this.api = null;
        this.available = false;
    }
}

