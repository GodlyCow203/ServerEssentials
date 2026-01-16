package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.config.BroadcastWorldConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class BroadcastWorldCommand implements CommandExecutor {
    private static final String PERMISSION = "essc.command.broadcastworld";

    private final PlayerLanguageManager langManager;
    private final BroadcastWorldConfig config;

    public BroadcastWorldCommand(PlayerLanguageManager langManager, BroadcastWorldConfig config) {
        this.langManager = langManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;

        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(langManager.getMessageFor(player, "commands.broadcastworld.no-permission",
                    "<red>You do not have permission to use this command!"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(langManager.getMessageFor(player, "commands.broadcastworld.usage",
                    "<red>Usage: /broadcastworld <world> <message>"));
            return true;
        }

        World world = Bukkit.getWorld(args[0]);
        if (world == null) {
            sender.sendMessage(langManager.getMessageFor(player, "commands.broadcastworld.world-not-found",
                    "<red>World <yellow>{world}</yellow> not found!",
                    ComponentPlaceholder.of("{world}", args[0])));
            return true;
        }

        StringBuilder message = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            message.append(args[i]).append(" ");
        }
        String finalMessage = message.toString().trim();

        Component broadcastMessage = langManager.getMessageFor(null, "commands.broadcastworld.broadcast",
                "{message}",
                ComponentPlaceholder.of("{message}", finalMessage));

        world.getPlayers().forEach(p -> p.sendMessage(broadcastMessage));

        sender.sendMessage(langManager.getMessageFor(player, "commands.broadcastworld.message-sent",
                "<green>Message broadcasted to world <yellow>{world}</yellow>!",
                ComponentPlaceholder.of("{world}", world.getName())));

        return true;
    }
}