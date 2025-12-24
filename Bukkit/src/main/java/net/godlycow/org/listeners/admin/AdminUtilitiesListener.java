package net.godlycow.org.listeners.admin;

import net.godlycow.org.commands.impl.GodCommand;
import net.godlycow.org.commands.impl.VanishCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class AdminUtilitiesListener implements Listener {

    private final Plugin plugin;
    private final GodCommand godCommand;
    private final VanishCommand vanishCommand;

    public AdminUtilitiesListener(Plugin plugin, GodCommand godCommand, VanishCommand vanishCommand) {
        this.plugin = plugin;
        this.godCommand = godCommand;
        this.vanishCommand = vanishCommand;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        godCommand.loadPlayerState(player.getUniqueId());
        vanishCommand.loadPlayerState(player.getUniqueId());

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (vanishCommand.isVanished(player.getUniqueId())) {
                plugin.getServer().getOnlinePlayers().forEach(p -> p.hidePlayer(plugin, player));
            }
        }, 10L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        godCommand.unloadPlayerState(event.getPlayer().getUniqueId());
        vanishCommand.unloadPlayerState(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (godCommand.isGodMode(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }
}
