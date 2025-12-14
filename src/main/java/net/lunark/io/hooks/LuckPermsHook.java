package net.lunark.io.hooks;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class LuckPermsHook {
    private static LuckPermsHook instance;
    private LuckPerms api = null;
    private boolean available = false;

    private LuckPermsHook() {}

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
            RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
            if (provider != null) {
                api = provider.getProvider();
            }
        } catch (Exception e) {
            try {
                api = LuckPermsProvider.get();
            } catch (Exception ex) {
                api = null;
            }
        }

        available = (api != null);
        return available;
    }

    public boolean isAvailable() {
        return available;
    }

    public LuckPerms getAPI() {
        return api;
    }

    public void cleanup() {
        api = null;
        available = false;
    }
}