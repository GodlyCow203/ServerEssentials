/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  net.milkbowl.vault.chat.Chat
 *  net.milkbowl.vault.economy.Economy
 *  net.milkbowl.vault.permission.Permission
 *  org.bukkit.Bukkit
 *  org.bukkit.plugin.RegisteredServiceProvider
 */
package net.lunark.io.hooks;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {
    private static VaultHook instance;
    private Economy economy = null;
    private Permission permission = null;
    private Chat chat = null;
    private boolean available = false;

    private VaultHook() {
    }

    public static VaultHook getInstance() {
        if (instance == null) {
            instance = new VaultHook();
        }
        return instance;
    }

    public boolean init() {
        RegisteredServiceProvider chatProvider;
        RegisteredServiceProvider permissionProvider;
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider economyProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            this.economy = (Economy)economyProvider.getProvider();
        }
        if ((permissionProvider = Bukkit.getServicesManager().getRegistration(Permission.class)) != null) {
            this.permission = (Permission)permissionProvider.getProvider();
        }
        if ((chatProvider = Bukkit.getServicesManager().getRegistration(Chat.class)) != null) {
            this.chat = (Chat)chatProvider.getProvider();
        }
        this.available = this.economy != null || this.permission != null || this.chat != null;
        return this.available;
    }

    public boolean isAvailable() {
        return this.available;
    }

    public Economy getEconomy() {
        return this.economy;
    }

    public Permission getPermission() {
        return this.permission;
    }

    public Chat getChat() {
        return this.chat;
    }

    public boolean hasEconomy() {
        return this.economy != null;
    }

    public boolean hasPermission() {
        return this.permission != null;
    }

    public boolean hasChat() {
        return this.chat != null;
    }

    public void cleanup() {
        this.economy = null;
        this.permission = null;
        this.chat = null;
        this.available = false;
    }
}

