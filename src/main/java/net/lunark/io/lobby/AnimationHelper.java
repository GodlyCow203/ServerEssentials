package net.lunark.io.lobby;

import net.lunark.io.commands.config.LobbyConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class AnimationHelper {

    public static void playTeleportAnimation(Plugin plugin, Player player, LobbyConfig.AnimationOptions options) {
        if (options == null) return;

        Location loc = player.getLocation();

        if (options.sound != null) {
            player.playSound(loc, options.sound, 1.0f, 1.0f);
        }

        switch (options.type.toUpperCase()) {
            case "FADE" -> playFadeAnimation(plugin, player, options);
            case "PARTICLE" -> playParticleAnimation(plugin, loc, options);
            case "BOTH" -> {
                playFadeAnimation(plugin, player, options);
                playParticleAnimation(plugin, loc, options);
            }
        }
    }

    private static void playFadeAnimation(Plugin plugin, Player player, LobbyConfig.AnimationOptions options) {
        player.sendTitle("§aTeleporting...", "", 10, options.duration, 20);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
        }, 5L);
    }

    private static void playParticleAnimation(Plugin plugin, Location loc, LobbyConfig.AnimationOptions options) {
        for (int i = 0; i < options.particleCount; i++) {
            double offsetX = (Math.random() - 0.5) * 2;
            double offsetY = Math.random() * 2;
            double offsetZ = (Math.random() - 0.5) * 2;

            loc.getWorld().spawnParticle(
                    options.particle,
                    loc.clone().add(offsetX, offsetY, offsetZ),
                    0,
                    0, 0, 0,
                    1.0
            );
        }
    }
}