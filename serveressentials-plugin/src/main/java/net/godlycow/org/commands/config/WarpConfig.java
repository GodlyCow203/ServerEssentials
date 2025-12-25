package net.godlycow.org.commands.config;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import java.time.Duration;

public class WarpConfig {
    private final Plugin plugin;
    public final Duration cooldown;
    public final int defaultMaxWarps;

    public WarpConfig(Plugin plugin) {
        this.plugin = plugin;
        this.cooldown = Duration.ofSeconds(plugin.getConfig().getLong("warp.cooldown", 0));
        this.defaultMaxWarps = plugin.getConfig().getInt("warp.max-per-player", 10);
    }

    public int getMaxWarpsForPlayer(Player player) {
        if (player.hasPermission("serveressentials.command.setwarp.unlimited")) {
            return Integer.MAX_VALUE;
        }
        for (int i = 100; i >= 1; i--) {
            if (player.hasPermission("serveressentials.command.setwarp." + i)) {
                return i;
            }
        }
        return defaultMaxWarps;
    }
}