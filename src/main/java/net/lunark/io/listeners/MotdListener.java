package net.lunark.io.listeners;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import net.lunark.io.ServerEssentials;

import java.io.File;
import java.util.List;

public class MotdListener implements Listener {

    private final ServerEssentials plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final int CHAT_LINE_WIDTH = 60;

    public MotdListener(ServerEssentials plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        File motdFile = new File(plugin.getDataFolder(), "motd.yml");
        if (!motdFile.exists()) return;

        var motdConfig = plugin.getServerMessages().getConfig();
        List<String> lines = motdConfig.getStringList("motd");
        if (lines == null || lines.isEmpty()) return;

        StringBuilder motdBuilder = new StringBuilder();

        for (String line : lines) {
            boolean centered = false;

            if (line.startsWith("[centered]")) {
                centered = true;
                line = line.replaceFirst("\\[centered\\]", "");
            }

            if (centered) {
                int spaces = (CHAT_LINE_WIDTH - line.length()) / 2;
                if (spaces > 0) line = " ".repeat(spaces) + line;
            }

            motdBuilder.append(line).append("\n");
        }

        event.setMotd(motdBuilder.toString().trim());
    }
}
