package serveressentials.serveressentials.interaction_blocks;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.ServerEssentials;

import java.io.File;

public class StonecutterCommand implements CommandExecutor {

    private final ServerEssentials plugin;
    private YamlConfiguration messages;
    private File messageFile;
    private final MiniMessage miniMessage;

    public StonecutterCommand(ServerEssentials plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        loadMessages();
    }

    /** Load messages from messages/interaction_blocks.yml */
    private void loadMessages() {
        messageFile = new File(plugin.getDataFolder(), "messages/interaction_blocks.yml");

        if (!messageFile.exists()) {
            plugin.saveResource("messages/interaction_blocks.yml", false);
        }

        messages = YamlConfiguration.loadConfiguration(messageFile);
    }

    /** Get a message from the config and parse MiniMessage formatting */
    private Component getMessage(String path) {
        String msg = messages.getString(path, "<red>Missing message: " + path + "</red>");
        return miniMessage.deserialize(msg);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            // Send message to console or non-player sender
            sender.sendMessage(miniMessage.deserialize("<red>Only players can use this command.</red>"));
            return true;
        }

        // Open the real Stonecutter GUI
        player.openStonecutter(null, true);

        // Send configurable MiniMessage message
        plugin.adventure().player(player).sendMessage(getMessage("opened-stonecutter"));
        return true;
    }
}
