package serveressentials.serveressentials;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WarpCooldowns {

    private static final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public static long getRemainingCooldown(UUID uuid, String warpName) {
        Map<String, Long> playerCooldowns = cooldowns.getOrDefault(uuid, new HashMap<>());
        long currentTime = System.currentTimeMillis() / 1000;
        long lastUsed = playerCooldowns.getOrDefault(warpName.toLowerCase(), 0L);
        long cooldown = WarpManager.getWarpData(warpName).getCooldownSeconds();
        long remaining = (lastUsed + cooldown) - currentTime;
        return Math.max(0, remaining);
    }

    public static void setCooldown(UUID uuid, String warpName, int seconds) {
        cooldowns.computeIfAbsent(uuid, k -> new HashMap<>())
                .put(warpName.toLowerCase(), System.currentTimeMillis() / 1000);
    }
}
