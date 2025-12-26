package net.godlycow.org.commands.config;

import org.bukkit.plugin.Plugin;

public final class BalanceTopConfig {
    private final int limit;

    public BalanceTopConfig(Plugin plugin) {
        this.limit = plugin.getConfig().getInt("balancetop.limit", 10);
    }

    public int getLimit() {
        return limit;
    }
}