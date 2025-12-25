package net.godlycow.org.commands.impl;

import net.kyori.adventure.text.Component;
import net.godlycow.org.commands.config.CoinFlipConfig;
import net.godlycow.org.economy.eco.EconomyManager;
import net.godlycow.org.economy.eco.EconomyResponse; // Added correct import
import net.godlycow.org.language.PlayerLanguageManager;
import net.godlycow.org.language.LanguageManager.ComponentPlaceholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public final class CoinFlipCommand implements CommandExecutor, TabCompleter {
    private static final String PERMISSION = "serveressentials.command.coinflip";
    private static final String COMMAND_NAME = "coinflip";
    private static final List<String> SUGGESTED_AMOUNTS = Arrays.asList("10", "50", "100", "500", "1000");

    private final JavaPlugin plugin;
    private final PlayerLanguageManager langManager;
    private final CoinFlipConfig config;
    private final EconomyManager economyManager;
    private final Random random;

    public CoinFlipCommand(JavaPlugin plugin, PlayerLanguageManager langManager, CoinFlipConfig config,
                           EconomyManager economyManager) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.config = config;
        this.economyManager = economyManager;
        this.random = new Random();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Component message = langManager.getComponent(null,
                    "commands." + COMMAND_NAME + ".only-player",
                    "<red>Only players can use this command.");
            sender.sendMessage(message);
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getComponent(String.valueOf(player),
                    "commands." + COMMAND_NAME + ".no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        if (!config.enabled) {
            player.sendMessage(langManager.getComponent(String.valueOf(player),
                    "commands." + COMMAND_NAME + ".disabled",
                    "<red>✗ Coin flip is currently disabled."));
            return true;
        }

        if (!economyManager.isEnabled()) {
            player.sendMessage(langManager.getComponent(String.valueOf(player),
                    "commands." + COMMAND_NAME + ".no-economy",
                    "<red>✗ Economy system is not available."));
            plugin.getLogger().warning("Coin flip attempted but economy system is disabled!");
            return true;
        }

        if (args.length != 1) {
            sendUsageMessage(player, label);
            return true;
        }
        double amount = parseAmount(args[0], player);
        if (amount < 0) return true;
        if (amount < config.minBetAmount) {
            player.sendMessage(langManager.getComponent(String.valueOf(player),
                    "commands." + COMMAND_NAME + ".bet-too-low",
                    "<red>Bet too low! Minimum is <yellow>{min}",
                    ComponentPlaceholder.of("{min}", economyManager.format(config.minBetAmount))));
            return true;
        }

        if (amount > config.maxBetAmount) {
            player.sendMessage(langManager.getComponent(String.valueOf(player),
                    "commands." + COMMAND_NAME + ".bet-too-high",
                    "<red>Bet too high! Maximum is <yellow>{max}",
                    ComponentPlaceholder.of("{max}", economyManager.format(config.maxBetAmount))));
            return true;
        }

        double balance = economyManager.getBalance(player);
        if (balance < amount) {
            player.sendMessage(langManager.getComponent(String.valueOf(player),
                    "commands." + COMMAND_NAME + ".not-enough",
                    "<red>You don't have enough money! You need <yellow>{needed}</yellow>, you have <green>{balance}",
                    ComponentPlaceholder.of("{needed}", economyManager.format(amount)),
                    ComponentPlaceholder.of("{balance}", economyManager.format(balance))));
            return true;
        }

        boolean win = random.nextDouble() < config.winChance;
        processCoinFlip(player, amount, win);

        return true;
    }


    private void sendUsageMessage(Player player, String label) {
        Component message = langManager.getComponent(String.valueOf(player),
                "commands." + COMMAND_NAME + ".usage",
                "<yellow>Usage: <white>{usage}\n<gray>Bet between <green>{min}</green> and <gold>{max}",
                ComponentPlaceholder.of("{usage}", "/" + label + " <amount>"),
                ComponentPlaceholder.of("{min}", economyManager.format(config.minBetAmount)),
                ComponentPlaceholder.of("{max}", economyManager.format(config.maxBetAmount)));
        player.sendMessage(message);
    }


    private double parseAmount(String input, Player player) {
        try {
            double amount = Double.parseDouble(input);
            if (amount <= 0) {
                player.sendMessage(langManager.getComponent(String.valueOf(player),
                        "commands." + COMMAND_NAME + ".positive-amount",
                        "<red>Amount must be positive!"));
                return -1;
            }
            return amount;
        } catch (NumberFormatException e) {
            player.sendMessage(langManager.getComponent(String.valueOf(player),
                    "commands." + COMMAND_NAME + ".invalid-amount",
                    "<red>Invalid amount: <white>{amount}",
                    ComponentPlaceholder.of("{amount}", input)));
            return -1;
        }
    }


    private void processCoinFlip(Player player, double amount, boolean win) {
        if (win) {
            EconomyResponse response = economyManager.deposit(player, amount);

            if (response.success()) {
                player.sendMessage(langManager.getComponent(String.valueOf(player),
                        "commands." + COMMAND_NAME + ".win",
                        "<green><bold>✓ YOU WIN!</bold>\n<green>You won <gold>{symbol}{amount}",
                        ComponentPlaceholder.of("{symbol}", getCurrencySymbol()),
                        ComponentPlaceholder.of("{amount}", economyManager.format(amount))));

                player.playSound(player.getLocation(),
                        org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
            } else {
                sendTransactionFailedMessage(player, response.errorMessage);
                return;
            }
        } else {
            EconomyResponse response = economyManager.withdraw(player, amount);

            if (response.success()) {
                player.sendMessage(langManager.getComponent(String.valueOf(player),
                        "commands." + COMMAND_NAME + ".lose",
                        "<red><bold>✗ YOU LOSE!</bold>\n<red>You lost <gold>{symbol}{amount}",
                        ComponentPlaceholder.of("{symbol}", getCurrencySymbol()),
                        ComponentPlaceholder.of("{amount}", economyManager.format(amount))));

                player.playSound(player.getLocation(),
                        org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 0.8f);
            } else {
                sendTransactionFailedMessage(player, response.errorMessage);
                return;
            }
        }

        if (config.logFlips) {
            String result = win ? "WON" : "LOST";
            plugin.getLogger().info(String.format("[CoinFlip] %s %s %.2f (%s economy)",
                    player.getName(), result, amount, economyManager.getEconomyName()));
        }
    }


    private void sendTransactionFailedMessage(Player player, String errorMessage) {
        player.sendMessage(langManager.getComponent(String.valueOf(player),
                "commands." + COMMAND_NAME + ".transaction-failed",
                "<red>✗ Transaction failed: {error}",
                ComponentPlaceholder.of("{error}", errorMessage)));

        plugin.getLogger().warning(String.format("[CoinFlip] Transaction failed for %s: %s",
                player.getName(), errorMessage));
    }


    private String getCurrencySymbol() {
        String formatted = economyManager.format(1.0);
        return formatted.replaceAll("[0-9.,]", "");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            return List.of();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            for (String amount : SUGGESTED_AMOUNTS) {
                if (amount.startsWith(input)) {
                    completions.add(amount);
                }
            }

            if (sender instanceof Player player && input.isEmpty()) {
                double balance = economyManager.getBalance(player);
                String balanceStr = String.valueOf((int) balance);
                if (!SUGGESTED_AMOUNTS.contains(balanceStr)) {
                    completions.add(balanceStr);
                }
            }
        }

        return completions;
    }
}