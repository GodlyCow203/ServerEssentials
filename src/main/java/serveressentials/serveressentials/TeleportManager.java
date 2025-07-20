package serveressentials.serveressentials;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class TeleportManager {
    private static final HashMap<UUID, UUID> requests = new HashMap<>();

    public static void sendRequest(Player sender, Player target) {
        requests.put(target.getUniqueId(), sender.getUniqueId());
    }

    public static Player getRequester(Player target) {
        UUID requesterId = requests.get(target.getUniqueId());
        return requesterId == null ? null : target.getServer().getPlayer(requesterId);
    }

    public static void removeRequest(Player target) {
        requests.remove(target.getUniqueId());
    }

    public static boolean hasRequest(Player target) {
        return requests.containsKey(target.getUniqueId());
    }
}
