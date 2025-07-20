package serveressentials.serveressentials;

import org.bukkit.Location;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BackManager {
    private static final Map<UUID, Location> backLocations = new HashMap<>();

    public static void setLastLocation(UUID uuid, Location location) {
        backLocations.put(uuid, location);
    }

    public static Location getLastLocation(UUID uuid) {
        return backLocations.get(uuid);
    }

    public static boolean hasBack(UUID uuid) {
        return backLocations.containsKey(uuid);
    }

    public static void clearBack(UUID uuid) {
        backLocations.remove(uuid);
    }
}
