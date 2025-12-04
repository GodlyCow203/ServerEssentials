package net.lunark.io.pw;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerWarp {

    private final UUID owner;
    private String name;
    private String description;
    private String category;
    private Location location;
    private Material icon;
    private long lastTeleportTime;
    private long cooldownTime = 60000;
    private int cooldownSeconds;
    private boolean isPublic = true;

    public PlayerWarp(UUID owner, String name, Location location) {
        this(owner, name, location, "Default");
    }

    public PlayerWarp(UUID owner, String name, Location location, String category) {
        this.owner = owner;
        this.name = name;
        this.location = location;
        this.description = "";
        this.category = category != null ? category : "Default";
        this.icon = Material.ENDER_PEARL;
        this.cooldownSeconds = 0;
    }

    public String getName() {
        return name;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Location getLocation() {
        return location;
    }

    public Material getIcon() {
        return icon;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setIcon(Material icon) {
        this.icon = icon;
    }

    public String getDescription() {
        return description;
    }

    public long getLastTeleportTime(Player player) {
        return this.lastTeleportTime;
    }

    public void setLastTeleportTime(Player player, long time) {
        this.lastTeleportTime = time;
    }

    public long getCooldownTime() {
        return this.cooldownTime;
    }

    public void setCooldownTime(long cooldownTime) {
        this.cooldownTime = cooldownTime;
    }

    public String getCategory() {
        return category;
    }

    public UUID getOwner() {
        return owner;
    }

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    public void setCooldownSeconds(int cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
    }
}
