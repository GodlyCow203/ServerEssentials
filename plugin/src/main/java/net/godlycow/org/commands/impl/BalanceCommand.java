package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.config.BalanceConfig;
import net.godlycow.org.economy.eco.EconomyManager;
import net.godlycow.org.language.PlayerLanguageManager;
import net.godlycow.org.language.LanguageManager.ComponentPlaceholder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class BalanceCommand implements CommandExecutor {
    private static final String PERMISSION_SELF = "essc.command.balance";
    private static final String PERMISSION_OTHERS = "essc.command.balance.others";
    private static final String COMMAND_NAME = "balance";

    private final PlayerLanguageManager langManager;
    private final BalanceConfig config;
    private final EconomyManager economyManager;

    public BalanceCommand(PlayerLanguageManager langManager, BalanceConfig config, EconomyManager economyManager) {
        this.langManager = langManager;
        this.config = config;
        this.economyManager = economyManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!economyManager.isEnabled()) {
            sender.sendMessage(langManager.getMessageFor(sender instanceof Player p ? p : null,
                    "commands." + COMMAND_NAME + ".no-economy",
                    "<red>§c✗ Economy system is not available."));
            return true;
        }

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

            double balance = economyManager.getBalance(player);
            player.sendMessage(langManager.getMessageFor(player,
                    "commands." + COMMAND_NAME + ".self",
                    "<green>Your balance is: <gold>{balance}",
                    ComponentPlaceholder.of("{balance}", economyManager.format(balance))));
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
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(langManager.getMessageFor(sender instanceof Player p ? p : null,
                    "commands." + COMMAND_NAME + ".player-not-found",
                    "<red>Player not found: <white>{player}",
                    ComponentPlaceholder.of("{player}", args[0])));
            return true;
        }

        double balance = economyManager.getEconomy().getBalance(target);

        sender.sendMessage(langManager.getMessageFor(sender instanceof Player p ? p : null,
                "commands." + COMMAND_NAME + ".other",
                "<green>{player}'s balance is: <gold>{balance}",
                ComponentPlaceholder.of("{player}", target.getName()),
                ComponentPlaceholder.of("{balance}", economyManager.format(balance))));
        return true;
    }
}