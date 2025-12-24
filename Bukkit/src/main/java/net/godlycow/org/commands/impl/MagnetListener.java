package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.config.MagnetConfig;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public final class MagnetListener implements Listener {
    private final MagnetConfig config;
    private final MagnetStorage storage;
    private final Plugin plugin;
    private BukkitRunnable magnetTask;

    public MagnetListener(Plugin plugin, MagnetConfig config, MagnetStorage storage) {
        this.plugin = plugin;
        this.config = config;
        this.storage = storage;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        startMagnetTask();
    }

    private void startMagnetTask() {
        magnetTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : storage.getActivePlayers()) {
                    if (!player.isOnline() || player.isDead()) {
                        storage.disableMagnet(player);
                        continue;
                    }

                    player.getWorld().getNearbyEntities(player.getLocation(), config.radius, config.radius, config.radius).stream()
                            .filter(entity -> entity instanceof Item)
                            .map(entity -> (Item) entity)
                            .filter(item -> !item.isGlowing())
                            .forEach(item -> {
                                Vector direction = player.getLocation().toVector()
                                        .subtract(item.getLocation().toVector())
                                        .normalize()
                                        .multiply(config.speed);
                                item.setVelocity(direction);
                            });
                }
            }
        };
        magnetTask.runTaskTimer(plugin, 0L, config.tickInterval);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        storage.onPlayerJoin(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        storage.onPlayerQuit(event.getPlayer());
    }

    public void stop() {
        if (magnetTask != null) {
            magnetTask.cancel();
        }
    }
}