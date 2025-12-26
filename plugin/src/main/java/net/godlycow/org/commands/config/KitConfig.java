package net.godlycow.org.commands.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class KitConfig {
    private final Plugin plugin;
    private String guiTitle;
    private int guiSize;
    private String cooldownMessage;
    private String noPermissionMessage;
    private String claimSuccessMessage;
    private String kitNotFoundMessage;
    private String previewTitle;

    public KitConfig(Plugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        FileConfiguration config = plugin.getConfig();

        this.guiTitle = config.getString("kits.gui.title", "<bold><green>Available Kits");
        this.guiSize = config.getInt("kits.gui.size", 54);
        this.previewTitle = config.getString("kits.gui.preview-title", "<white>Preview Kit: <yellow>{kit}");

        this.cooldownMessage = config.getString("kits.messages.cooldown",
                "<red>Please wait <yellow>{time}s</yellow> before claiming <white>{kit}</white> again!");
        this.noPermissionMessage = config.getString("kits.messages.no-permission",
                "<red>You don't have permission to claim <white>{kit}</white>!");
        this.claimSuccessMessage = config.getString("kits.messages.claim-success",
                "<green>✓ Successfully claimed kit <white>{kit}</white>!");
        this.kitNotFoundMessage = config.getString("kits.messages.kit-not-found",
                "<red>Kit <white>{kit}</white> not found!");
    }

    public String getGuiTitle() { return guiTitle; }
    public int getGuiSize() { return guiSize; }
    public String getPreviewTitle() { return previewTitle; }
    public String getCooldownMessage() { return cooldownMessage; }
    public String getNoPermissionMessage() { return noPermissionMessage; }
    public String getClaimSuccessMessage() { return claimSuccessMessage; }
    public String getKitNotFoundMessage() { return kitNotFoundMessage; }
}