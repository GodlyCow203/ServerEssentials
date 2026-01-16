package net.godlycow.org.commands.config;

import org.bukkit.plugin.Plugin;

import java.util.List;

public final class NickConfig {
    public static final String PERMISSION = "essc.command.nick";
    public static final String ADMIN_PERMISSION = "essc.command.nicks";

    public boolean enabled;
    public int minLength;
    public int maxLength;
    public boolean allowFormatting;
    public boolean allowReset;
    public boolean allowDuplicates;
    public int cooldown;
    public int maxChangesPerDay;
    public List<String> blockedWords;
    public List<String> blacklistPatterns;

    private final Plugin plugin;

    public NickConfig(Plugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        enabled = plugin.getConfig().getBoolean("nick.enabled", true);
        minLength = plugin.getConfig().getInt("nick.min-length", 3);
        maxLength = plugin.getConfig().getInt("nick.max-length", 16);
        allowFormatting = plugin.getConfig().getBoolean("nick.allow-formatting", true);
        allowReset = plugin.getConfig().getBoolean("nick.allow-reset", true);
        allowDuplicates = plugin.getConfig().getBoolean("nick.allow-duplicates", false);
        cooldown = plugin.getConfig().getInt("nick.cooldown", 60);
        maxChangesPerDay = plugin.getConfig().getInt("nick.max-changes-per-day", 3);
        blockedWords = plugin.getConfig().getStringList("nick.blocked-words");
        blacklistPatterns = plugin.getConfig().getStringList("nick.blacklist-patterns");
    }
}