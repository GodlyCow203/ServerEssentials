package net.lunark.io.server;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import net.lunark.io.ServerEssentials;
import net.lunark.io.util.ServerMessages;

import java.io.File;
import java.util.List;

public class MotdCommand implements CommandExecutor {

    private final ServerMessages serverMessages;
    private final File motdFile;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final int CHAT_LINE_WIDTH = 60;

    public MotdCommand(ServerEssentials plugin) {
        this.serverMessages = new ServerMessages(plugin, "messages/server.yml");
        this.motdFile = new File(plugin.getDataFolder(), "motd.yml");

        serverMessages.addDefault("motd.reload-success", "<green>MOTD reloaded from motd.yml and will be shown on ping.");
        serverMessages.addDefault("motd.file-created", "<yellow>Default motd.yml created.");
        serverMessages.addDefault("motd.file-missing", "<red>Error: motd.yml is missing from both plugin folder and JAR resources.");
        serverMessages.addDefault("motd.file-empty", "<red>Your motd.yml is empty or malformed.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!motdFile.exists()) {
            sender.sendMessage(serverMessages.get("motd.file-created"));
            return true;
        }

        List<String> lines = serverMessages.getConfig().getStringList("motd");
        if (lines == null || lines.isEmpty()) {
            sender.sendMessage(serverMessages.get("motd.file-empty"));
            return true;
        }

        for (String line : lines) {
            boolean centered = false;

            if (line.startsWith("[centered]")) {
                centered = true;
                line = line.replaceFirst("\\[centered\\]", "");
            }

            line = line.replace("{player}", sender.getName());

            if (centered) {
                int spaces = (CHAT_LINE_WIDTH - line.length()) / 2;
                if (spaces > 0) {
                    String padding = " ".repeat(spaces);
                    line = padding + line;
                }
            }

            Component message = miniMessage.deserialize(line);
            sender.sendMessage(message);
        }

        sender.sendMessage(serverMessages.get("motd.reload-success"));
        return true;
    }
}
