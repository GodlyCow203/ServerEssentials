package net.lunark.io.TPA;

import org.bukkit.plugin.Plugin;

public class TPAConfig {
    private final Plugin plugin;

    public final int cooldown;
    public final int timeout;
    public final int warmup;
    public final int teleportDelay;
    public final boolean cancelOnMove;
    public final boolean blockMoveThresholdBlocks;

    public final boolean chatEnabled;
    public final boolean actionbarEnabled;
    public final boolean bossbarEnabled;
    public final String bossbarColor;
    public final String bossbarStyle;
    public final int bossbarDuration;
    public final boolean titleEnabled;

    public final boolean crossWorld;
    public final java.util.List<String> blockedWorlds;

    public final boolean economyEnabled;
    public final double costTpa;
    public final double costTpahere;
    public final double costTpall;
    public final boolean refundOnDeny;
    public final boolean refundOnExpire;

    public final boolean particlesEnabled;
    public final String particleType;
    public final String soundRequest;
    public final String soundAccept;
    public final String soundDeny;
    public final String soundTeleport;

    public TPAConfig(Plugin plugin) {
        this.plugin = plugin;


        cooldown = getInt("settings.cooldown", 5);
        timeout = getInt("settings.timeout", 60);
        warmup = getInt("settings.warmup", 0);
        teleportDelay = getInt("teleport-delay", 3);
        cancelOnMove = getBool("settings.cancel-on-move", true);
        blockMoveThresholdBlocks = getBool("settings.block-move-threshold-blocks", true);

        chatEnabled = getBool("notifications.chat", true);
        actionbarEnabled = getBool("notifications.actionbar", false);
        bossbarEnabled = getBool("notifications.bossbar", false);
        bossbarColor = plugin.getConfig().getString("tpa.notifications.bossbar-color", "BLUE");
        bossbarStyle = plugin.getConfig().getString("tpa.notifications.bossbar-style", "SOLID");
        bossbarDuration = Math.max(1, getInt("notifications.bossbar-duration", 5));
        titleEnabled = getBool("notifications.title", false);

        crossWorld = getBool("restrictions.cross-world", false);
        blockedWorlds = plugin.getConfig().getStringList("tpa.restrictions.blocked-worlds");

        economyEnabled = getBool("economy.enabled", false);
        costTpa = getDouble("economy.cost.tpa", 0.0);
        costTpahere = getDouble("economy.cost.tpahere", 0.0);
        costTpall = getDouble("economy.cost.tpall", 0.0);
        refundOnDeny = getBool("economy.refund-on-deny", true);
        refundOnExpire = getBool("economy.refund-on-expire", true);

        particlesEnabled = getBool("particles.enabled", true);
        particleType = plugin.getConfig().getString("tpa.particles.type", "PORTAL");
        soundRequest = plugin.getConfig().getString("tpa.sounds.request", "ENTITY_EXPERIENCE_ORB_PICKUP");
        soundAccept = plugin.getConfig().getString("tpa.sounds.accept", "ENTITY_EXPERIENCE_ORB_PICKUP");
        soundDeny = plugin.getConfig().getString("tpa.sounds.deny", "ENTITY_EXPERIENCE_ORB_PICKUP");
        soundTeleport = plugin.getConfig().getString("tpa.sounds.teleport", "ENTITY_EXPERIENCE_ORB_PICKUP");
    }

    private int getInt(String path, int def) {
        return plugin.getConfig().getInt("tpa." + path, def);
    }

    private boolean getBool(String path, boolean def) {
        return plugin.getConfig().getBoolean("tpa." + path, def);
    }

    private double getDouble(String path, double def) {
        return plugin.getConfig().getDouble("tpa." + path, def);
    }
}