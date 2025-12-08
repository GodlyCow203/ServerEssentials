package net.lunark.io.commands.impl;

import net.lunark.io.commands.config.BalanceConfig;
import net.lunark.io.economy.ServerEssentialsEconomy;
import net.lunark.io.language.PlayerLanguageManager;
import net.lunark.io.language.LanguageManager.ComponentPlaceholder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class BalanceCommand implements CommandExecutor {
    private static final String PERMISSION_SELF = "serveressentials.command.balance";
    private static final String PERMISSION_OTHERS = "serveressentials.command.balance.others";
    private static final String COMMAND_NAME = "balance";

    private final PlayerLanguageManager langManager;
    private final BalanceConfig config;
    private final ServerEssentialsEconomy economy;

    public BalanceCommand(PlayerLanguageManager langManager, BalanceConfig config, ServerEssentialsEconomy economy) {
        this.langManager = langManager;
        this.config = config;
        this.economy = economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(langManager.getMessageFor(null,
                        "commands." + COMMAND_NAME + ".only-player",
                        "<red>Only players can check their own balance.").toString());
                return true;
            }

            if (!player.hasPermission(PERMISSION_SELF)) {
                player.sendMessage(langManager.getMessageFor(player,
                        "commands." + COMMAND_NAME + ".no-permission",
                        "<red>You need permission <yellow>{permission}</yellow>!",
                        ComponentPlaceholder.of("{permission}", PERMISSION_SELF)));
                return true;
            }

            double balance = economy.getBalance(player);
            player.sendMessage(langManager.getMessageFor(player,
                    "commands." + COMMAND_NAME + ".self",
                    "<green>Your balance is: <gold>{balance}",
                    ComponentPlaceholder.of("{balance}", String.format("%.2f", balance))));
            return true;
        }

        if (!sender.hasPermission(PERMISSION_OTHERS)) {
            sender.sendMessage(langManager.getMessageFor(sender instanceof Player p ? p : null,
                    "commands." + COMMAND_NAME + ".no-permission-others",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION_OTHERS)));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
            sender.sendMessage(langManager.getMessageFor(sender instanceof Player p ? p : null,
                    "commands." + COMMAND_NAME + ".player-not-found",
                    "<red>Player not found: <white>{player}",
                    ComponentPlaceholder.of("{player}", args[0])));
            return true;
        }

        double balance = economy.getBalance(target);
        sender.sendMessage(langManager.getMessageFor(sender instanceof Player p ? p : null,
                "commands." + COMMAND_NAME + ".other",
                "<green>{player}'s balance is: <gold>{balance}",
                ComponentPlaceholder.of("{player}", target.getName()),
                ComponentPlaceholder.of("{balance}", String.format("%.2f", balance))));
        return true;
    }
}