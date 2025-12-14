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

    private VaultHook() {}

    public static VaultHook getInstance() {
        if (instance == null) {
            instance = new VaultHook();
        }
        return instance;
    }

    public boolean init() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServicesManager().getRegistration(Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }

        RegisteredServiceProvider<Chat> chatProvider = Bukkit.getServicesManager().getRegistration(Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }

        available = (economy != null || permission != null || chat != null);
        return available;
    }

    public boolean isAvailable() {
        return available;
    }

    public Economy getEconomy() {
        return economy;
    }

    public Permission getPermission() {
        return permission;
    }

    public Chat getChat() {
        return chat;
    }

    public boolean hasEconomy() {
        return economy != null;
    }

    public boolean hasPermission() {
        return permission != null;
    }

    public boolean hasChat() {
        return chat != null;
    }

    public void cleanup() {
        economy = null;
        permission = null;
        chat = null;
        available = false;
    }
}