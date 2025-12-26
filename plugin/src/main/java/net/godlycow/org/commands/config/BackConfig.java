package net.godlycow.org.commands.config;

import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.ConfigurationSection;


public final class BackConfig {
    private final boolean saveOnTeleport;
    private final boolean saveOnDeath;
    private final boolean saveOnQuit;
    private final boolean saveOnRespawn;
    private final String lobbyWorld;
    private final double lobbyX;
    private final double lobbyY;
    private final double lobbyZ;
    private final float lobbyYaw;
    private final float lobbyPitch;

    public BackConfig(Plugin plugin) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("back");
        if (section == null) {
            this.saveOnTeleport = true;
            this.saveOnDeath = true;
            this.saveOnQuit = true;
            this.saveOnRespawn = true;
            this.lobbyWorld = "world";
            this.lobbyX = 0.0;
            this.lobbyY = 100.0;
            this.lobbyZ = 0.0;
            this.lobbyYaw = 0.0f;
            this.lobbyPitch = 0.0f;
            return;
        }

        this.saveOnTeleport = section.getBoolean("save-on-teleport", true);
        this.saveOnDeath = section.getBoolean("save-on-death", true);
        this.saveOnQuit = section.getBoolean("save-on-quit", true);
        this.saveOnRespawn = section.getBoolean("save-on-respawn", true);

        ConfigurationSection lobby = section.getConfigurationSection("lobby");
        if (lobby != null) {
            this.lobbyWorld = lobby.getString("world", "world");
            this.lobbyX = lobby.getDouble("x", 0.0);
            this.lobbyY = lobby.getDouble("y", 100.0);
            this.lobbyZ = lobby.getDouble("z", 0.0);
            this.lobbyYaw = (float) lobby.getDouble("yaw", 0.0);
            this.lobbyPitch = (float) lobby.getDouble("pitch", 0.0);
        } else {
            this.lobbyWorld = "world";
            this.lobbyX = 0.0;
            this.lobbyY = 100.0;
            this.lobbyZ = 0.0;
            this.lobbyYaw = 0.0f;
            this.lobbyPitch = 0.0f;
        }
    }

    public boolean saveOnTeleport() { return saveOnTeleport; }
    public boolean saveOnDeath() { return saveOnDeath; }
    public boolean saveOnQuit() { return saveOnQuit; }
    public boolean saveOnRespawn() { return saveOnRespawn; }
    public String lobbyWorld() { return lobbyWorld; }
    public double lobbyX() { return lobbyX; }
    public double lobbyY() { return lobbyY; }
    public double lobbyZ() { return lobbyZ; }
    public float lobbyYaw() { return lobbyYaw; }
    public float lobbyPitch() { return lobbyPitch; }
}