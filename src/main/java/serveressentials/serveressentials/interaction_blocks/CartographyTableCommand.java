package serveressentials.serveressentials.interaction_blocks;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.ServerEssentials;

import java.io.File;

public class CartographyTableCommand implements CommandExecutor {

    private final ServerEssentials plugin;
    private YamlConfiguration messages;
    private File messageFile;

    public CartographyTableCommand(ServerEssentials plugin) {
        this.plugin = plugin;
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

    /** Get a message from the config */
    private String getMessage(String path) {
        String msg = messages.getString(path, "&cMissing message: " + path);
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(getMessage("only-players"));
            return true;
        }

        // Open the real Cartography Table GUI
        player.openCartographyTable(null, true);

        // Send configurable message
        player.sendMessage(getMessage("opened-cartography-table"));
        return true;
    }
}
