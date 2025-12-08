package net.lunark.io.commands.impl;

import net.lunark.io.commands.config.CoinFlipConfig;
import net.lunark.io.economy.ServerEssentialsEconomy;
import net.lunark.io.language.PlayerLanguageManager;
import net.lunark.io.language.LanguageManager.ComponentPlaceholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class CoinFlipCommand implements CommandExecutor, TabCompleter {
    private static final String PERMISSION = "serveressentials.command.coinflip";
    private static final String COMMAND_NAME = "coinflip";
    private static final List<String> SUGGESTED_AMOUNTS = Arrays.asList("10", "50", "100", "500", "1000");

    private final PlayerLanguageManager langManager;
    private final CoinFlipConfig config;
    private final ServerEssentialsEconomy economy;
    private final Random random;

    public CoinFlipCommand(PlayerLanguageManager langManager, CoinFlipConfig config, ServerEssentialsEconomy economy) {
        this.langManager = langManager;
        this.config = config;
        this.economy = economy;
        this.random = new Random();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null,
                    "commands." + COMMAND_NAME + ".only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player,
                    "commands." + COMMAND_NAME + ".no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(langManager.getMessageFor(player,
                    "commands." + COMMAND_NAME + ".usage",
                    "<yellow>Usage: <white>{usage}",
                    ComponentPlaceholder.of("{usage}", "/" + label + " <amount>")));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(langManager.getMessageFor(player,
                    "commands." + COMMAND_NAME + ".invalid-amount",
                    "<red>Invalid amount: <white>{amount}",
                    ComponentPlaceholder.of("{amount}", args[0])));
            return true;
        }

        if (amount <= 0) {
            player.sendMessage(langManager.getMessageFor(player,
                    "commands." + COMMAND_NAME + ".positive-amount",
                    "<red>Amount must be positive!"));
            return true;
        }

        double balance = economy.getBalance(player);
        if (balance < amount) {
            player.sendMessage(langManager.getMessageFor(player,
                    "commands." + COMMAND_NAME + ".not-enough",
                    "<red>You don't have enough money! You need <yellow>{needed}</yellow>, you have <green>{balance}",
                    ComponentPlaceholder.of("{needed}", economy.format(amount)),
                    ComponentPlaceholder.of("{balance}", economy.format(balance))));
            return true;
        }

        boolean win = random.nextBoolean();
        if (win) {
            economy.depositPlayer(player, amount);
            player.sendMessage(langManager.getMessageFor(player,
                    "commands." + COMMAND_NAME + ".win",
                    "<green>You won! You received <gold>{amount}",
                    ComponentPlaceholder.of("{amount}", economy.format(amount))));
        } else {
            economy.withdrawPlayer(player, amount);
            player.sendMessage(langManager.getMessageFor(player,
                    "commands." + COMMAND_NAME + ".lose",
                    "<red>You lost! You lost <gold>{amount}",
                    ComponentPlaceholder.of("{amount}", economy.format(amount))));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            return List.of();
        }

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();
            for (String s : SUGGESTED_AMOUNTS) {
                if (s.startsWith(input)) completions.add(s);
            }
            return completions;
        }
        return List.of();
    }
}