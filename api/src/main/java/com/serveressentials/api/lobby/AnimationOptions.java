package com.serveressentials.api.lobby;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;


public final class AnimationOptions {
    private final @NotNull String type;
    private final int duration;
    private final @NotNull Particle particle;
    private final @NotNull Sound sound;
    private final int particleCount;

    public AnimationOptions(@NotNull String type, int duration, @NotNull Particle particle,
                            @NotNull Sound sound, int particleCount) {
        this.type = Objects.requireNonNull(type, "type cannot be null");
        this.duration = duration;
        this.particle = Objects.requireNonNull(particle, "particle cannot be null");
        this.sound = Objects.requireNonNull(sound, "sound cannot be null");
        this.particleCount = particleCount;
    }

    public @NotNull String getType() {
        return type;
    }

    public int getDuration() {
        return duration;
    }

    public @NotNull Particle getParticle() {
        return particle;
    }

    public @NotNull Sound getSound() {
        return sound;
    }

    public int getParticleCount() {
        return particleCount;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AnimationOptions)) return false;
        AnimationOptions that = (AnimationOptions) obj;
        return duration == that.duration &&
                particleCount == that.particleCount &&
                type.equals(that.type) &&
                particle.equals(that.particle) &&
                sound.equals(that.sound);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, duration, particle, sound, particleCount);
    }

    @Override
    public String toString() {
        return "AnimationOptions{" +
                "type='" + type + '\'' +
                ", duration=" + duration +
                ", particle=" + particle +
                ", sound=" + sound +
                ", particleCount=" + particleCount +
                '}';
    }
}