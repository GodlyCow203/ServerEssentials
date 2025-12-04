package net.lunark.io.staff;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.lunark.io.ServerEssentials;
import net.lunark.io.util.MessagesManager;

public class UnmuteCommand implements CommandExecutor {

    private final MessagesManager messages;

    public UnmuteCommand(ServerEssentials plugin) {
        this.messages = new MessagesManager(plugin);

        // Default messages for unmute (MiniMessage format)
        messages.addDefault("unmute.no-permission", "<red>You don't have permission.");
        messages.addDefault("unmute.usage", "<red>Usage: /unmute <player>");
        messages.addDefault("unmute.player-not-found", "<red>Player <#00ffff>%player%</#00ffff> not found.");
        messages.addDefault("unmute.unmuted", "<green>Successfully unmuted <#00ffff>%player%</#00ffff>");
        messages.addDefault("unmute.not-muted", "<red>That player is not muted.");
        messages.addDefault("unmute.notify", "<green>You have been unmuted!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("serveressentials.unmute")) {
            sender.sendMessage(messages.getMessageComponent("unmute.no-permission"));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(messages.getMessageComponent("unmute.usage"));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(messages.getMessageComponent("unmute.player-not-found", "%player%", args[0]));
            return true;
        }

        if (MuteCommand.isMuted(target.getUniqueId())) {
            // ✅ Pass MessagesManager to unmute method
            MuteCommand.unmute(target.getUniqueId(), messages);

            sender.sendMessage(messages.getMessageComponent("unmute.unmuted", "%player%", target.getName()));
        } else {
            sender.sendMessage(messages.getMessageComponent("unmute.not-muted", "%player%", target.getName()));
        }

        return true;
    }
}
