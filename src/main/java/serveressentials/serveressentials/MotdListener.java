package serveressentials.serveressentials;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import java.io.File;
import java.util.List;

public class MotdListener implements Listener {

    private final ServerEssentials plugin;

    public MotdListener(ServerEssentials plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        File motdFile = new File(plugin.getDataFolder(), "motd.yml");
        if (!motdFile.exists()) return;

        FileConfiguration motdConfig = YamlConfiguration.loadConfiguration(motdFile);
        List<String> lines = motdConfig.getStringList("motd");

        if (lines == null || lines.isEmpty()) return;

        StringBuilder motd = new StringBuilder();
        for (String line : lines) {
            motd.append(MotdCommand.parseHexColor(line)).append("\n");
        }

        event.setMotd(motd.toString().trim());
    }
}
