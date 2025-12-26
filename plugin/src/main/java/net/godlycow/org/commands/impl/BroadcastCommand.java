package net.godlycow.org.commands.impl;

import net.kyori.adventure.text.Component;
import net.godlycow.org.commands.config.BroadcastConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class BroadcastCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.broadcast";

    private final PlayerLanguageManager langManager;
    private final BroadcastConfig config;

    public BroadcastCommand(PlayerLanguageManager langManager, BroadcastConfig config) {
        this.langManager = langManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;

        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(langManager.getMessageFor(player, "commands.broadcast.no-permission",
                    "<red>You do not have permission to use this command!"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(langManager.getMessageFor(player, "commands.broadcast.usage",
                    "<red>Usage: /broadcast <message>"));
            return true;
        }

        String message = String.join(" ", args);
        Component broadcastMessage = langManager.getMessageFor(null, "commands.broadcast.format",
                "<blue>[Server] <white>{message}",
                ComponentPlaceholder.of("{message}", message));

        Bukkit.broadcast(broadcastMessage);
        return true;
    }
}