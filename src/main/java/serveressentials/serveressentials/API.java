package serveressentials.serveressentials;

import org.bukkit.Bukkit;

public class API {

    private final String serverId;

    public API(String serverId) {
        this.serverId = serverId;
    }

    public String getServerId() {
        return serverId;
    }

    public int getOnlinePlayers() {
        return Bukkit.getOnlinePlayers().size();
    }

    public int getMaxPlayers() {
        return Bukkit.getMaxPlayers();
    }

    public int getRAMUsage() {
        long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        return (int) (usedMemory / (1024 * 1024));
    }

    public int getTotalRAM() {
        return (int) (Runtime.getRuntime().totalMemory() / (1024 * 1024));
    }

    public int getFreeRAM() {
        return (int) (Runtime.getRuntime().freeMemory() / (1024 * 1024));
    }
}