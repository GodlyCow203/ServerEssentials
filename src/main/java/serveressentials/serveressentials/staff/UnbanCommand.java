package serveressentials.serveressentials.staff;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.MessagesManager;

import java.util.UUID;

public class UnbanCommand implements CommandExecutor {

    private final BanManager banManager;
    private final MessagesManager messages;

    public UnbanCommand(ServerEssentials plugin, BanManager banManager) {
        this.banManager = banManager;
        this.messages = new MessagesManager(plugin);

        // Add default messages for unban (MiniMessage format)
        messages.addDefault("unban.no-permission", "<red>You don't have permission.");
        messages.addDefault("unban.usage", "<red>Usage: /unban <player>");
        messages.addDefault("unban.player-not-found", "<red>Could not find banned player with name '<yellow>%player%</yellow>'.");
        messages.addDefault("unban.not-banned", "<yellow>%player%</yellow> is not currently banned.");
        messages.addDefault("unban.success", "<green>%player%</green> has been unbanned.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("ban.use")) {
            sender.sendMessage(messages.getMessageComponent("unban.no-permission"));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(messages.getMessageComponent("unban.usage"));
            return true;
        }

        String playerName = args[0];
        UUID uuid = banManager.getUUIDFromName(playerName);

        if (uuid == null) {
            sender.sendMessage(messages.getMessageComponent("unban.player-not-found", "%player%", playerName));
            return true;
        }

        if (!banManager.isBanned(uuid)) {
            sender.sendMessage(messages.getMessageComponent("unban.not-banned", "%player%", playerName));
            return true;
        }

        banManager.unbanPlayer(uuid);
        sender.sendMessage(messages.getMessageComponent("unban.success", "%player%", playerName));

        return true;
    }
}
