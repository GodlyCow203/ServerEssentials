package net.godlycow.org.commands.config;

import org.bukkit.plugin.Plugin;
import java.util.List;
import java.util.logging.Level;

public class HomesConfig {
    private final Plugin plugin;

    public int maxHomes;
    public boolean allowRename;
    public int setCooldownSeconds;
    public int teleportCooldownSeconds;
    public List<String> disabledWorlds;
    public int maxHomeNameLength;
    public boolean allowDuplicateHomeNames;
    public boolean requireEmptyInventoryToSet;
    public boolean allowCrossWorldTeleport;
    public List<String> restrictedWorldTeleports;

    public HomesConfig(Plugin plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        try {
            this.maxHomes = Math.max(1, plugin.getConfig().getInt("homes.max-homes", 8));
            this.allowRename = plugin.getConfig().getBoolean("homes.allow-rename", true);
            this.setCooldownSeconds = Math.max(0, plugin.getConfig().getInt("homes.cooldowns.set", 30));
            this.teleportCooldownSeconds = Math.max(0, plugin.getConfig().getInt("homes.cooldowns.teleport", 5));
            this.disabledWorlds = plugin.getConfig().getStringList("homes.restrictions.disabled-worlds");
            this.restrictedWorldTeleports = plugin.getConfig().getStringList("homes.restrictions.teleport-restricted-worlds");
            this.allowCrossWorldTeleport = plugin.getConfig().getBoolean("homes.restrictions.allow-cross-world-teleport", true);
            this.maxHomeNameLength = Math.max(1, Math.min(50, plugin.getConfig().getInt("homes.naming.max-length", 20)));
            this.allowDuplicateHomeNames = plugin.getConfig().getBoolean("homes.naming.allow-duplicate-names", true);
            this.requireEmptyInventoryToSet = plugin.getConfig().getBoolean("homes.restrictions.require-empty-inventory", false);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load homes config", e);
        }
    }

    public boolean isWorldDisabled(String worldName) {
        return worldName != null && disabledWorlds.stream()
                .anyMatch(disabled -> disabled.equalsIgnoreCase(worldName));
    }

    public boolean isTeleportRestricted(String worldName) {
        return worldName != null && restrictedWorldTeleports.stream()
                .anyMatch(restricted -> restricted.equalsIgnoreCase(worldName));
    }

    public void reload() {
        plugin.reloadConfig();
        load();
        plugin.getLogger().info("[ServerEssentials] Homes config reloaded");
    }
}