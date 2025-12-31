package com.serveressentials.api.tpa;

import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Objects;

public final class TPASettings {
    private final int cooldown;
    private final int timeout;
    private final int warmup;
    private final int teleportDelay;
    private final boolean cancelOnMove;
    private final boolean blockMoveThresholdBlocks;
    private final boolean crossWorld;
    private final @NotNull List<String> blockedWorlds;
    private final boolean economyEnabled;
    private final boolean particlesEnabled;
    private final @NotNull String particleType;

    public TPASettings(int cooldown, int timeout, int warmup, int teleportDelay,
                       boolean cancelOnMove, boolean blockMoveThresholdBlocks,
                       boolean crossWorld, @NotNull List<String> blockedWorlds,
                       boolean economyEnabled, boolean particlesEnabled, @NotNull String particleType) {
        this.cooldown = cooldown;
        this.timeout = timeout;
        this.warmup = warmup;
        this.teleportDelay = teleportDelay;
        this.cancelOnMove = cancelOnMove;
        this.blockMoveThresholdBlocks = blockMoveThresholdBlocks;
        this.crossWorld = crossWorld;
        this.blockedWorlds = Objects.requireNonNull(blockedWorlds, "blockedWorlds cannot be null");
        this.economyEnabled = economyEnabled;
        this.particlesEnabled = particlesEnabled;
        this.particleType = Objects.requireNonNull(particleType, "particleType cannot be null");
    }

    public int getCooldown() {
        return cooldown;
    }

    public int getTimeout() {
        return timeout;
    }

    public int getWarmup() {
        return warmup;
    }

    public int getTeleportDelay() {
        return teleportDelay;
    }

    public boolean isCancelOnMove() {
        return cancelOnMove;
    }

    public boolean isBlockMoveThresholdBlocks() {
        return blockMoveThresholdBlocks;
    }

    public boolean isCrossWorld() {
        return crossWorld;
    }

    public @NotNull List<String> getBlockedWorlds() {
        return blockedWorlds;
    }

    public boolean isEconomyEnabled() {
        return economyEnabled;
    }

    public boolean isParticlesEnabled() {
        return particlesEnabled;
    }

    public @NotNull String getParticleType() {
        return particleType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TPASettings that = (TPASettings) o;
        return cooldown == that.cooldown && timeout == that.timeout && warmup == that.warmup &&
                teleportDelay == that.teleportDelay && cancelOnMove == that.cancelOnMove &&
                blockMoveThresholdBlocks == that.blockMoveThresholdBlocks && crossWorld == that.crossWorld &&
                economyEnabled == that.economyEnabled && particlesEnabled == that.particlesEnabled &&
                Objects.equals(blockedWorlds, that.blockedWorlds) && Objects.equals(particleType, that.particleType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cooldown, timeout, warmup, teleportDelay, cancelOnMove,
                blockMoveThresholdBlocks, crossWorld, blockedWorlds, economyEnabled, particlesEnabled, particleType);
    }

    @Override
    public @NotNull String toString() {
        return "TPASettings{" + "cooldown=" + cooldown + ", timeout=" + timeout + ", warmup=" + warmup +
                ", teleportDelay=" + teleportDelay + ", cancelOnMove=" + cancelOnMove +
                ", blockMoveThresholdBlocks=" + blockMoveThresholdBlocks + ", crossWorld=" + crossWorld +
                ", blockedWorlds=" + blockedWorlds + ", economyEnabled=" + economyEnabled +
                ", particlesEnabled=" + particlesEnabled + ", particleType='" + particleType + '\'' + '}';
    }
}