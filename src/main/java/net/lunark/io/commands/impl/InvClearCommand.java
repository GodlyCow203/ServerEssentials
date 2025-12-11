package net.lunark.io.commands.impl;

import net.lunark.io.commands.config.InvClearConfig;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static net.lunark.io.language.LanguageManager.ComponentPlaceholder;

public final class InvClearCommand implements TabExecutor {
    private static final String PERMISSION = "serveressentials.command.invclear";

    private final PlayerLanguageManager langManager;
    private final InvClearConfig config;

    public InvClearCommand(PlayerLanguageManager langManager, InvClearConfig config) {
        this.langManager = langManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.invclear.only-player", "<red>Only players can use this command!"));
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.invclear.no-permission", "<red>You need permission <yellow>{permission}</yellow>!", ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        Player target = player;
        if (args.length >= 1) {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(langManager.getMessageFor(player, "commands.invclear.player-not-found", "<red>Player not found: <yellow>{player}</yellow>", ComponentPlaceholder.of("{player}", args[0])));
                return true;
            }
        }

        target.getInventory().clear();
        target.sendMessage(langManager.getMessageFor(target, "commands.invclear.cleared", "<green>Your inventory has been cleared."));

        if (!target.equals(player)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.invclear.cleared-other", "<green>Cleared <white>{player}</white>'s inventory.", ComponentPlaceholder.of("{player}", target.getName())));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return Collections.emptyList();
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}