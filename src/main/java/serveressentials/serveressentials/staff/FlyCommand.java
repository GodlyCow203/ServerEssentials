package serveressentials.serveressentials.staff;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.MessagesManager;

public class FlyCommand implements CommandExecutor {

    private final MessagesManager messages;

    public FlyCommand(ServerEssentials plugin) {
        this.messages = new MessagesManager(plugin);

        // Default messages for /fly in MiniMessage format
        messages.addDefault("fly.only-players", "<red>Only players can use this command.");
        messages.addDefault("fly.enabled", "<blue>Flight <green>enabled<blue>.");
        messages.addDefault("fly.disabled", "<blue>Flight <red>disabled<blue>.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            // Console or non-player
            sendMessage(sender, "fly.only-players");
            return true;
        }

        boolean flying = player.getAllowFlight();
        player.setAllowFlight(!flying);
        player.setFlying(!flying);

        if (flying) {
            sendMessage(player, "fly.disabled");
        } else {
            sendMessage(player, "fly.enabled");
        }

        return true;
    }

    // Sends a message as Adventure Component to players,
    // but falls back to legacy string for console
    private void sendMessage(CommandSender sender, String path) {
        Component message = messages.getMessageComponent(path);
        if (sender instanceof Player p) {
            p.sendMessage(message); // full MiniMessage formatting
        } else {
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().serialize(message));
        }
    }
}
