package net.lunark.io.staff;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.lunark.io.ServerEssentials;
import net.lunark.io.util.MessagesManager;

public class PlayerInfoCommand implements CommandExecutor {

    private final MessagesManager messages;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public PlayerInfoCommand(ServerEssentials plugin) {
        this.messages = new MessagesManager(plugin);

        messages.addDefault("playerinfo.usage", "<red>Usage: /playerinfo <player>");
        messages.addDefault("playerinfo.not-found", "<red>Player not found.");
        messages.addDefault("playerinfo.header", "<gold>----- Player Info -----");
        messages.addDefault("playerinfo.name", "<yellow>Name: %player%");
        messages.addDefault("playerinfo.uuid", "<yellow>UUID: %uuid%");
        messages.addDefault("playerinfo.health", "<yellow>Health: %health%/%maxhealth%");
        messages.addDefault("playerinfo.food", "<yellow>Food: %food%");
        messages.addDefault("playerinfo.location", "<yellow>Location: %location%");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendMessage(sender, "playerinfo.usage");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sendMessage(sender, "playerinfo.not-found");
            return true;
        }

        sendMessage(sender, "playerinfo.header");

        // Player info
        sendMessage(sender, "playerinfo.name", target);
        sendMessage(sender, "playerinfo.uuid", target);
        sendMessage(sender, "playerinfo.health", target);
        sendMessage(sender, "playerinfo.food", target);
        sendMessage(sender, "playerinfo.location", target);

        return true;
    }

    // Sends a message without placeholders
    private void sendMessage(CommandSender sender, String path) {
        Component message = messages.getMessageComponent(path);
        if (sender instanceof Player p) {
            p.sendMessage(message);
        } else {
            // Console fallback
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().serialize(message));
        }
    }

    // Sends a message and replaces placeholders for a target player
    private void sendMessage(CommandSender sender, String path, Player target) {
        Component message = messages.getMessageComponent(path)
                .replaceText(builder -> builder.matchLiteral("%player%").replacement(target.getName()))
                .replaceText(builder -> builder.matchLiteral("%uuid%").replacement(target.getUniqueId().toString()))
                .replaceText(builder -> builder.matchLiteral("%health%").replacement(String.valueOf(target.getHealth())))
                .replaceText(builder -> builder.matchLiteral("%maxhealth%").replacement(String.valueOf(target.getMaxHealth())))
                .replaceText(builder -> builder.matchLiteral("%food%").replacement(String.valueOf(target.getFoodLevel())))
                .replaceText(builder -> builder.matchLiteral("%location%").replacement(target.getLocation().toVector().toString()));

        if (sender instanceof Player p) {
            p.sendMessage(message);
        } else {
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().serialize(message));
        }
    }
}
