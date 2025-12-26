package net.godlycow.org.commands.config;

import org.bukkit.plugin.Plugin;


public final class NukeConfig {
    private final float explosionPower;
    private final boolean setFire;
    private final boolean breakBlocks;

    public NukeConfig(Plugin plugin) {
        this.explosionPower = (float) plugin.getConfig().getDouble("nuke.power", 5.0);
        this.setFire = plugin.getConfig().getBoolean("nuke.fire", true);
        this.breakBlocks = plugin.getConfig().getBoolean("nuke.break-blocks", true);
    }

    public float explosionPower() { return explosionPower; }
    public boolean setFire() { return setFire; }
    public boolean breakBlocks() { return breakBlocks; }
}