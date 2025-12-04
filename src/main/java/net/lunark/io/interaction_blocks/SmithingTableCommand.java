package net.lunark.io.interaction_blocks;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import net.lunark.io.ServerEssentials;

import java.io.File;

public class SmithingTableCommand implements CommandExecutor {

    private final ServerEssentials plugin;
    private YamlConfiguration messages;
    private File messageFile;
    private final MiniMessage miniMessage;

    public SmithingTableCommand(ServerEssentials plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        loadMessages();
    }

    private void loadMessages() {
        messageFile = new File(plugin.getDataFolder(), "messages/interaction_blocks.yml");

        if (!messageFile.exists()) {
            plugin.saveResource("messages/interaction_blocks.yml", false);
        }

        messages = YamlConfiguration.loadConfiguration(messageFile);
    }

    private Component getMessage(String path) {
        String msg = messages.getString(path, "<red>Missing message: " + path + "</red>");
        return miniMessage.deserialize(msg);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(miniMessage.deserialize("<red>Only players can use this command.</red>"));
            return true;
        }

        player.openSmithingTable(null, true);

        plugin.adventure().player(player).sendMessage(getMessage("opened-smithing-table"));
        return true;
    }
}
