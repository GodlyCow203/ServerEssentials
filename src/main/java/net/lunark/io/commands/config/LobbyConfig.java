package net.lunark.io.commands.config;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.plugin.Plugin;
import java.time.Duration;

public class LobbyConfig {
    public final Plugin plugin;

    public final Duration cooldown;
    public final boolean animationEnabled;
    public final AnimationOptions animation;
    public final boolean perWorld;
    public final boolean teleportOnJoin;

    public static class AnimationOptions {
        public final String type;
        public final int duration;
        public final Particle particle;
        public final Sound sound;
        public final int particleCount;

        public AnimationOptions(Plugin plugin) {
            this.type = plugin.getConfig().getString("lobby.animation.type", "FADE");
            this.duration = plugin.getConfig().getInt("lobby.animation.duration", 40);
            String particleName = plugin.getConfig().getString("lobby.animation.particle", "PORTAL");
            this.particle = Particle.valueOf(particleName);
            String soundName = plugin.getConfig().getString("lobby.animation.sound", "ENTITY_ENDERMAN_TELEPORT");
            this.sound = Sound.valueOf(soundName);
            this.particleCount = plugin.getConfig().getInt("lobby.animation.particle-count", 50);
        }
    }

    public LobbyConfig(Plugin plugin) {
        this.plugin = plugin;
        this.cooldown = Duration.ofSeconds(plugin.getConfig().getLong("lobby.cooldown", 5L));
        this.animationEnabled = plugin.getConfig().getBoolean("lobby.animation.enabled", true);
        this.animation = animationEnabled ? new AnimationOptions(plugin) : null;
        this.perWorld = plugin.getConfig().getBoolean("lobby.per-world", false);
        this.teleportOnJoin = plugin.getConfig().getBoolean("lobby.teleport-on-join", false);
    }
}