package serveressentials.serveressentials.Player;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.util.PlayerMessages;

public class NearCommand implements CommandExecutor {

    private final PlayerMessages messages;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public NearCommand(PlayerMessages messages) {
        this.messages = messages;

        // Default messages (only set if missing)
        messages.addDefault("Near.Messages.PlayerOnly", "<red>Only players can use this command.");
        messages.addDefault("Near.Messages.Header", "<green>Players near you:");
        messages.addDefault("Near.Messages.Entry", "<yellow>- <name> <gray>(<distance> blocks)");
        messages.addDefault("Near.Messages.None", "<red>No players are nearby.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("Near.Messages.PlayerOnly"));
            return true;
        }

        Location loc = player.getLocation();
        player.sendMessage(messages.get("Near.Messages.Header"));

        boolean found = false;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(player) && p.getLocation().distance(loc) <= 20) {
                found = true;

                String entry = messages.getConfig()
                        .getString("Near.Messages.Entry", "- <name> (<distance> blocks)")
                        .replace("<name>", p.getName())
                        .replace("<distance>", String.valueOf((int) p.getLocation().distance(loc)));

                player.sendMessage(miniMessage.deserialize(entry));
            }
        }

        if (!found) {
            player.sendMessage(messages.get("Near.Messages.None"));
        }

        return true;
    }
}
