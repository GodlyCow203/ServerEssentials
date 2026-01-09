package net.godlycow.org.commands.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class KitConfig {
    private final Plugin plugin;
    private String guiTitle;
    private int guiSize;
    private String previewTitle;
    private Material claimButtonMaterial;
    private Material cooldownButtonMaterial;
    private Material backButtonMaterial;

    public KitConfig(Plugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        FileConfiguration config = plugin.getConfig();

        this.guiTitle = config.getString("kits.gui.title", "<bold><green>Available Kits");
        this.guiSize = config.getInt("kits.gui.size", 54);
        this.previewTitle = config.getString("kits.gui.preview-title", "<white>Preview Kit: <yellow>{kit}");

        this.claimButtonMaterial = Material.valueOf(
                config.getString("kits.gui.buttons.claim.material", "LIME_CONCRETE").toUpperCase()
        );
        this.cooldownButtonMaterial = Material.valueOf(
                config.getString("kits.gui.buttons.cooldown.material", "RED_CONCRETE").toUpperCase()
        );
        this.backButtonMaterial = Material.valueOf(
                config.getString("kits.gui.buttons.back.material", "BARRIER").toUpperCase()
        );
    }

    public String getGuiTitle() { return guiTitle; }
    public int getGuiSize() { return guiSize; }
    public String getPreviewTitle() { return previewTitle; }
    public Material getClaimButtonMaterial() { return claimButtonMaterial; }
    public Material getCooldownButtonMaterial() { return cooldownButtonMaterial; }
    public Material getBackButtonMaterial() { return backButtonMaterial; }
}