package serveressentials.serveressentials.server;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.*;
import serveressentials.serveressentials.util.ServerMessages;

public class BroadcastWorldCommand implements CommandExecutor {

    private final ServerMessages messages;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public BroadcastWorldCommand(ServerMessages messages) {
        this.messages = messages;

        // Default messages
        messages.addDefault("BroadcastWorld.Usage", "Usage: /broadcastworld <world> <message>");
        messages.addDefault("BroadcastWorld.WorldNotFound", "World '{world}' not found!");
        messages.addDefault("BroadcastWorld.MessageSent", "Message broadcasted to world '{world}'!");
        messages.addDefault("BroadcastWorld.Broadcast", "{message}");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length < 2) {
            Component usage = miniMessage.deserialize(
                    messages.getConfig().getString("BroadcastWorld.Usage")
            );
            sender.sendMessage(usage);
            return true;
        }

        World world = Bukkit.getWorld(args[0]);
        if (world == null) {
            Component notFound = miniMessage.deserialize(
                    messages.getConfig().getString("BroadcastWorld.WorldNotFound")
                            .replace("{world}", args[0])
            );
            sender.sendMessage(notFound);
            return true;
        }

        // Build the message
        StringBuilder rawMessage = new StringBuilder();
        for (int i = 1; i < args.length; i++) rawMessage.append(args[i]).append(" ");

        Component broadcast = miniMessage.deserialize(
                messages.getConfig().getString("BroadcastWorld.Broadcast")
                        .replace("{message}", rawMessage.toString().trim())
        );

        world.getPlayers().forEach(p -> p.sendMessage(broadcast));

        String confirmationRaw = messages.getConfig().getString("BroadcastWorld.MessageSent")
                .replace("{world}", world.getName());
        sender.sendMessage(miniMessage.deserialize(confirmationRaw));

        return true;
    }
}
