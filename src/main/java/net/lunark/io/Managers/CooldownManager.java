package net.lunark.io.Managers;

import java.util.HashMap;
import java.util.UUID;

public class CooldownManager {
    private static final HashMap<UUID, Long> cooldowns = new HashMap<>();

    public static boolean isOnCooldown(UUID uuid) {
        return cooldowns.containsKey(uuid) && System.currentTimeMillis() < cooldowns.get(uuid);
    }

    public static long getRemaining(UUID uuid) {
        return (cooldowns.getOrDefault(uuid, 0L) - System.currentTimeMillis()) / 1000;
    }

    public static void setCooldown(UUID uuid, int seconds) {
        cooldowns.put(uuid, System.currentTimeMillis() + seconds * 1000L);
    }
}
