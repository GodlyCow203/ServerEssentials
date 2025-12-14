package net.lunark.io.hooks;

import org.bukkit.plugin.java.JavaPlugin;

public class HooksManager {
    private static HooksManager instance;
    private final JavaPlugin plugin;
    private boolean vaultActive = false;
    private boolean luckPermsActive = false;
    private boolean papiActive = false;

    private HooksManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public static HooksManager getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new HooksManager(plugin);
        }
        return instance;
    }

    public void initializeHooks() {
        plugin.getLogger().info("Initializing soft-dependencies...");

        try {
            vaultActive = VaultHook.getInstance().init();
            if (vaultActive) {
                plugin.getLogger().info("§aVault hooked successfully!");
            } else {
                plugin.getLogger().warning("§eVault not found, related features disabled.");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("§eFailed to hook Vault: " + e.getMessage());
            vaultActive = false;
        }

        try {
            luckPermsActive = LuckPermsHook.getInstance().init();
            if (luckPermsActive) {
                plugin.getLogger().info("§aLuckPerms hooked successfully!");
            } else {
                plugin.getLogger().warning("§eLuckPerms not found, related features disabled.");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("§eFailed to hook LuckPerms: " + e.getMessage());
            luckPermsActive = false;
        }

        try {
            papiActive = PlaceholderAPIHook.getInstance().init();
            if (papiActive) {
                plugin.getLogger().info("§aPlaceholderAPI hooked successfully!");
            } else {
                plugin.getLogger().warning("§ePlaceholderAPI not found, placeholders won't work.");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("§eFailed to hook PlaceholderAPI: " + e.getMessage());
            papiActive = false;
        }
    }

    public void cleanupHooks() {
        VaultHook.getInstance().cleanup();
        LuckPermsHook.getInstance().cleanup();
        PlaceholderAPIHook.getInstance().cleanup();
    }

    public VaultHook getVault() {
        return vaultActive ? VaultHook.getInstance() : null;
    }

    public LuckPermsHook getLuckPerms() {
        return luckPermsActive ? LuckPermsHook.getInstance() : null;
    }

    public PlaceholderAPIHook getPlaceholderAPI() {
        return papiActive ? PlaceholderAPIHook.getInstance() : null;
    }

    public boolean isVaultActive() {
        return vaultActive;
    }

    public boolean isLuckPermsActive() {
        return luckPermsActive;
    }

    public boolean isPlaceholderAPIActive() {
        return papiActive;
    }

    public boolean hasAnyHook() {
        return vaultActive || luckPermsActive || papiActive;
    }
}