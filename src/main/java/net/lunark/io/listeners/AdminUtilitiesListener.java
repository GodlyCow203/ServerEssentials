package net.lunark.io.listeners;

import net.lunark.io.commands.impl.GodCommand;
import net.lunark.io.commands.impl.VanishCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class AdminUtilitiesListener implements Listener {
    private final Plugin plugin;

    public AdminUtilitiesListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        VanishCommand.loadPlayerState(player.getUniqueId());
        GodCommand.loadPlayerState(player.getUniqueId());

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (VanishCommand.isVanished(player.getUniqueId())) {
                plugin.getServer().getOnlinePlayers().forEach(p -> p.hidePlayer(plugin, player));
            }
        }, 10L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        VanishCommand.unloadPlayerState(event.getPlayer().getUniqueId());
        GodCommand.unloadPlayerState(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (GodCommand.isGodMode(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }
}