package net.godlycow.org.commands.config;

import org.bukkit.plugin.Plugin;
import java.util.List;

public final class AFKConfig {
    public boolean enabled;
    public long timeoutSeconds;
    public boolean kickOnAFK;
    public boolean teleportOnAFK;
    public String teleportWorld;
    public double teleportX;
    public double teleportY;
    public double teleportZ;
    public boolean soundsEnabled;
    public String soundName;
    public boolean particlesEnabled;
    public String particleName;
    public boolean bossbarEnabled;
    public String bossbarText;
    public String bossbarColor;
    public String bossbarStyle;
    public boolean broadcastEnabled;
    public boolean disableFlight;
    public boolean disableDamage;
    public boolean disableItemPickup;
    public boolean actionBarEnabled;
    public String actionBarText;
    public boolean modifyTablist;
    public String tablistPrefix;
    public String tablistSuffix;
    public List<String> exemptWorlds;
    public String enterMessage;
    public String leaveMessage;
    public String kickMessage;

    private final Plugin plugin;

    public AFKConfig(Plugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        enabled = plugin.getConfig().getBoolean("afk.enabled", true);
        timeoutSeconds = plugin.getConfig().getLong("afk.timeout-seconds", 300);
        kickOnAFK = plugin.getConfig().getBoolean("afk.kick-on-afk", false);
        teleportOnAFK = plugin.getConfig().getBoolean("afk.teleport.enabled", false);
        teleportWorld = plugin.getConfig().getString("afk.teleport.world", "world");
        teleportX = plugin.getConfig().getDouble("afk.teleport.x", 0.0);
        teleportY = plugin.getConfig().getDouble("afk.teleport.y", 64.0);
        teleportZ = plugin.getConfig().getDouble("afk.teleport.z", 0.0);
        soundsEnabled = plugin.getConfig().getBoolean("afk.sounds.enabled", true);
        soundName = plugin.getConfig().getString("afk.sounds.sound", "BLOCK_NOTE_BLOCK_BELL");
        particlesEnabled = plugin.getConfig().getBoolean("afk.particles.enabled", true);
        particleName = plugin.getConfig().getString("afk.particles.particle", "CRIT");
        bossbarEnabled = plugin.getConfig().getBoolean("afk.bossbar.enabled", false);
        bossbarText = plugin.getConfig().getString("afk.bossbar.text", "<yellow>%player% is AFK");
        bossbarColor = plugin.getConfig().getString("afk.bossbar.color", "YELLOW");
        bossbarStyle = plugin.getConfig().getString("afk.bossbar.style", "SEGMENTED_20");
        broadcastEnabled = plugin.getConfig().getBoolean("afk.broadcast.enabled", true);
        disableFlight = plugin.getConfig().getBoolean("afk.disable-flight", true);
        disableDamage = plugin.getConfig().getBoolean("afk.disable-damage", false);
        disableItemPickup = plugin.getConfig().getBoolean("afk.disable-item-pickup", true);
        actionBarEnabled = plugin.getConfig().getBoolean("afk.actionbar.enabled", false);
        actionBarText = plugin.getConfig().getString("afk.actionbar.text", "<gray>%player% is AFK");
        modifyTablist = plugin.getConfig().getBoolean("afk.modify-tablist", false);
        tablistPrefix = plugin.getConfig().getString("afk.tablist.prefix", "<yellow>[AFK] ");
        tablistSuffix = plugin.getConfig().getString("afk.tablist.suffix", "");
        exemptWorlds = plugin.getConfig().getStringList("afk.exempt-worlds");
        enterMessage = plugin.getConfig().getString("afk.messages.enter", "<yellow>%player% is now AFK");
        leaveMessage = plugin.getConfig().getString("afk.messages.leave", "<yellow>%player% is no longer AFK");
        kickMessage = plugin.getConfig().getString("afk.messages.kick", "<red>You were kicked for being AFK");
    }
}