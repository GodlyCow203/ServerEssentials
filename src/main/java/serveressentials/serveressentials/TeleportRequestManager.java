package serveressentials.serveressentials;

import org.bukkit.entity.Player;

import java.util.*;

public class TeleportRequestManager {
    // Map of target → list of players who requested to teleport to them
    private static final Map<UUID, Set<UUID>> requests = new HashMap<>();

    public static void addRequest(Player from, Player to) {
        requests.computeIfAbsent(to.getUniqueId(), k -> new HashSet<>()).add(from.getUniqueId());
    }

    public static Set<UUID> getRequests(UUID targetUUID) {
        return requests.getOrDefault(targetUUID, Collections.emptySet());
    }

    public static boolean hasRequestFrom(UUID targetUUID, UUID requesterUUID) {
        return requests.containsKey(targetUUID) && requests.get(targetUUID).contains(requesterUUID);
    }

    public static boolean removeRequest(UUID targetUUID, UUID requesterUUID) {
        Set<UUID> list = requests.get(targetUUID);
        if (list != null) {
            boolean removed = list.remove(requesterUUID);
            if (list.isEmpty()) requests.remove(targetUUID);
            return removed;
        }
        return false;
    }

    public static void clearRequests(UUID targetUUID) {
        requests.remove(targetUUID);
    }
}
