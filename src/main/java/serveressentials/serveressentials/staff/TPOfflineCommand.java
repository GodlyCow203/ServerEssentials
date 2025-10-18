package serveressentials.serveressentials.staff;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.MessagesManager;

public class TPOfflineCommand implements CommandExecutor {

    private final ServerEssentials plugin;
    private final MessagesManager messages;

    public TPOfflineCommand(ServerEssentials plugin) {
        this.plugin = plugin;
        this.messages = new MessagesManager(plugin);

        // Add default messages for TPOffline (MiniMessage format)
        messages.addDefault("tpoffline.usage", "<red>Usage: /tpoffline <player>");
        messages.addDefault("tpoffline.offline-location", "<red>Player is offline and location is unavailable.");
        messages.addDefault("tpoffline.teleported", "<green>Teleported to <#ffff00>%player%</#ffff00>");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(messages.getMessageComponent("tpoffline.usage"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        Location loc = null;

        if (target.isOnline()) {
            loc = target.getPlayer().getLocation();
        } else {
            loc = plugin.getLastLocation(target.getUniqueId());
        }

        if (loc == null) {
            player.sendMessage(messages.getMessageComponent("tpoffline.offline-location"));
        } else {
            player.teleport(loc);
            // Use placeholder replacement
            player.sendMessage(messages.getMessageComponent("tpoffline.teleported", "%player%", target.getName()));
        }

        return true;
    }
}
