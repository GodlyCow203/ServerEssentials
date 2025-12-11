package net.lunark.io.commands.config;

import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;

public final class PowerToolConfig {
    private final int maxUses;
    private final Set<Material> allowedMaterials;

    public PowerToolConfig(Plugin plugin) {
        this.maxUses = plugin.getConfig().getInt("powertool.max-uses", 100);
        this.allowedMaterials = new HashSet<>();
        for (String matName : plugin.getConfig().getStringList("powertool.allowed-materials")) {
            Material mat = Material.matchMaterial(matName.toUpperCase());
            if (mat != null) {
                allowedMaterials.add(mat);
            } else {
                plugin.getLogger().warning("Invalid material in powertool.allowed-materials: " + matName);
            }
        }

        if (allowedMaterials.isEmpty()) {
            addDefaultMaterials();
        }
    }

    private void addDefaultMaterials() {
        allowedMaterials.add(Material.STICK);
        allowedMaterials.add(Material.BLAZE_ROD);
        allowedMaterials.add(Material.BONE);
        allowedMaterials.add(Material.ENDER_PEARL);
        allowedMaterials.add(Material.EXPERIENCE_BOTTLE);
        allowedMaterials.add(Material.CLOCK);
        allowedMaterials.add(Material.COMPASS);
    }

    public int maxUses() { return maxUses; }

    public boolean isAllowedMaterial(Material material) {
        return allowedMaterials.contains(material);
    }
}