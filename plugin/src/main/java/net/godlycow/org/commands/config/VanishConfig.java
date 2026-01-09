package net.godlycow.org.commands.config;

import org.bukkit.plugin.Plugin;

public final class VanishConfig {

    private final boolean showInTabList;

    public VanishConfig(Plugin plugin) {
        plugin.saveDefaultConfig();

        this.showInTabList = plugin
                .getConfig()
                .getBoolean("vanish.show-in-tab-list", false);
    }

    public boolean showVanishedInTabList() {
        return showInTabList;
    }
}
