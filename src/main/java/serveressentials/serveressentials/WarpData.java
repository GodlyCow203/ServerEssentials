package serveressentials.serveressentials;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static serveressentials.serveressentials.WarpManager.warps;

public class WarpData {

    private String name;
    private Location location;
    private boolean enabled;
    private Material material;
    private String category;
    private List<String> lore;
    private long cooldownSeconds;
    private String description;

    // Constructor for creating a new warp
    public WarpData(String name, Location location) {
        this.name = name;
        this.location = location;
        this.enabled = true;
        this.material = Material.ENDER_PEARL;
        this.category = "default";
        this.lore = new ArrayList<>();
        this.cooldownSeconds = 60;
        this.description = "";
    }

    // Constructor for loading from config
    public WarpData(String name, Location location, boolean enabled, String category, Material material, List<String> lore, long cooldownSeconds) {
        this.name = name;
        this.location = location;
        this.enabled = enabled;
        this.material = material;
        this.category = category;
        this.lore = (lore != null) ? lore : new ArrayList<>();
        this.cooldownSeconds = cooldownSeconds;
        this.description = "";
    }

    // === Getters and Setters ===

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getLore() {
        return lore;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public long getCooldown() {
        return cooldownSeconds;
    }

    public void setCooldown(long cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
    }

    public int getCooldownSeconds() {
        return (int) cooldownSeconds;
    }

    public void setCooldownSeconds(int seconds) {
        this.cooldownSeconds = seconds;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Optional: If you're calling this from outside WarpManager
    public static Collection<WarpData> getAllWarps() {
        return warps.values();
    }
}
