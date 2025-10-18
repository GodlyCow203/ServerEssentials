package serveressentials.serveressentials.staff;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.MessagesManager;

public class ClearChatCommand implements CommandExecutor {

    private final MessagesManager messages;

    public ClearChatCommand(ServerEssentials plugin) {
        this.messages = new MessagesManager(plugin);

        // Add default messages for clear chat (MiniMessage format)
        messages.addDefault("clearchat.no-permission", "<red>You do not have permission to use this command.");
        messages.addDefault("clearchat.cleared", "<gray>Chat has been cleared by <#ff0000>%player%</#ff0000>");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("serveressentials.clearchat")) {
            sender.sendMessage(messages.getMessageComponent("clearchat.no-permission"));
            return true;
        }

        String clearedBy = (sender instanceof Player)
                ? ((Player) sender).getDisplayName()
                : "Console";

        for (Player player : Bukkit.getOnlinePlayers()) {
            // Flood chat with blank lines
            for (int i = 0; i < 150; i++) {
                player.sendMessage("");
            }

            // Notify chat cleared with placeholder replacement
            Component msg = messages.getMessageComponent("clearchat.cleared", "%player%", clearedBy);
            player.sendMessage(msg);
        }

        return true;
    }
}
