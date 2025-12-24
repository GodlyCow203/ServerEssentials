package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.config.UnfreezeConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class UnfreezeCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.unfreeze";

    private final PlayerLanguageManager langManager;
    private final UnfreezeConfig config;

    public UnfreezeCommand(PlayerLanguageManager langManager, UnfreezeConfig config) {
        this.langManager = langManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;

        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(langManager.getMessageFor(player, "commands.unfreeze.no-permission",
                    "<red>You do not have permission to use this command!"));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(langManager.getMessageFor(player, "commands.unfreeze.usage",
                    "<red>Usage: /unfreeze <player>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(langManager.getMessageFor(player, "commands.unfreeze.player-not-found",
                    "<red>Player not found: <yellow>{player}</yellow>",
                    ComponentPlaceholder.of("{player}", args[0])));
            return true;
        }

        boolean wasFrozen = FreezeCommand.getFrozenPlayers().remove(target.getUniqueId());
        if (!wasFrozen) {
            player.sendMessage(langManager.getMessageFor(player, "commands.unfreeze.not-frozen",
                    "<red>Player <yellow>{player}</yellow> is not frozen!",
                    ComponentPlaceholder.of("{player}", target.getName())));
            return true;
        }

        player.sendMessage(langManager.getMessageFor(player, "commands.unfreeze.success",
                "<green>Unfroze <white>{player}</white>.",
                ComponentPlaceholder.of("{player}", target.getName())));

        target.sendMessage(langManager.getMessageFor(target, "commands.unfreeze.target-unfrozen",
                "<green>You have been unfrozen."));

        return true;
    }
}