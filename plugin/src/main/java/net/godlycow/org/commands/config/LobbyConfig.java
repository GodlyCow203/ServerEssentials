package net.godlycow.org.commands.config;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.plugin.Plugin;
import java.time.Duration;


public final class LobbyConfig {
    public final Plugin plugin;

    private Duration cooldown;
    private boolean animationEnabled;
    private AnimationOptions animation;
    private boolean perWorld;
    private boolean teleportOnJoin;

    public static class AnimationOptions {
        public String type;
        public int duration;
        public Particle particle;
        public Sound sound;
        public int particleCount;

        public AnimationOptions(Plugin plugin) {
            reload(plugin);
        }

        public void reload(Plugin plugin) {
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
        reload();
    }

    public void reload() {
        this.cooldown = Duration.ofSeconds(plugin.getConfig().getLong("lobby.cooldown", 5L));
        this.animationEnabled = plugin.getConfig().getBoolean("lobby.animation.enabled", true);
        if (this.animation == null) {
            this.animation = new AnimationOptions(plugin);
        } else {
            this.animation.reload(plugin);
        }
        this.perWorld = plugin.getConfig().getBoolean("lobby.per-world", false);
        this.teleportOnJoin = plugin.getConfig().getBoolean("lobby.teleport-on-join", false);
    }

    public Duration getCooldown() { return cooldown; }
    public boolean isAnimationEnabled() { return animationEnabled; }
    public AnimationOptions getAnimation() { return animation; }
    public boolean isPerWorld() { return perWorld; }
    public boolean isTeleportOnJoin() { return teleportOnJoin; }
}