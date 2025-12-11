package net.lunark.io.commands.config;

import org.bukkit.plugin.Plugin;

public class HomesConfig {
    private final Plugin plugin;

    public final int maxHomes;
    public final String guiTitleMain;
    public final String guiTitleConfirm;
    public final boolean allowRename;

    public HomesConfig(Plugin plugin) {
        this.plugin = plugin;
        this.maxHomes = plugin.getConfig().getInt("homes.max-homes", 8);
        this.guiTitleMain = plugin.getConfig().getString("homes.gui.titles.main", "<green>Home Manager");
        this.guiTitleConfirm = plugin.getConfig().getString("homes.gui.titles.confirm", "<red>Confirm Action");
        this.allowRename = plugin.getConfig().getBoolean("homes.allow-rename", true);
    }
}