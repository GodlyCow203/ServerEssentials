package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.config.EcoConfig;
import net.godlycow.org.economy.eco.EconomyManager;
import net.godlycow.org.economy.eco.EconomyResponse; // Correct import
import net.godlycow.org.language.PlayerLanguageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;


public final class EcoCommand implements CommandExecutor, TabCompleter {
    private static final String PERMISSION = "serveressentials.command.eco";
    private static final String COMMAND_NAME = "eco";

    private final PlayerLanguageManager langManager;
    private final EcoConfig config;
    private final EconomyManager economyManager;
    private final JavaPlugin plugin;

    public EcoCommand(JavaPlugin plugin, PlayerLanguageManager langManager, EcoConfig config,
                      EconomyManager economyManager) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.config = config;
        this.economyManager = economyManager;
    }


    private OfflinePlayer getTargetPlayer(String name) {
        Player online = Bukkit.getPlayer(name);
        if (online != null) return online;

        OfflinePlayer offline = Bukkit.getOfflinePlayerIfCached(name);
        if (offline == null) {
            offline = Bukkit.getOfflinePlayer(name);
        }
        return offline;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            Component message = langManager.getComponent(String.valueOf(sender instanceof Player p ? p : null),
                    "commands." + COMMAND_NAME + ".no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION));
            sender.sendMessage(message);
            return true;
        }

        if (!economyManager.isEnabled()) {
            sender.sendMessage(langManager.getComponent(String.valueOf(sender instanceof Player p ? p : null),
                    "commands." + COMMAND_NAME + ".no-economy",
                    "<red>✗ Economy system is not available!"));
            return true;
        }

        if (args.length < 2) {
            sendUsageMessage(sender, label);
            return true;
        }

        OfflinePlayer target = getTargetPlayer(args[1]);
        if (target.getName() == null) {
            Component message = langManager.getComponent(String.valueOf(sender instanceof Player p ? p : null),
                    "commands." + COMMAND_NAME + ".player-not-found",
                    "<red>Player not found: <white>{player}",
                    ComponentPlaceholder.of("{player}", args[1]));
            sender.sendMessage(message);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "give" -> handleGive(sender, target, args, label);
            case "take" -> handleTake(sender, target, args, label);
            case "reset" -> handleReset(sender, target);
            case "set" -> handleSet(sender, target, args, label);
            default -> {
                Component message = langManager.getComponent(String.valueOf(sender instanceof Player p ? p : null),
                        "commands." + COMMAND_NAME + ".unknown-subcommand",
                        "<red>Unknown subcommand: <white>{sub}\n<gray>Available: give, take, set, reset",
                        ComponentPlaceholder.of("{sub}", sub));
                sender.sendMessage(message);
            }
        }

        return true;
    }


    private void sendUsageMessage(CommandSender sender, String label) {
        Component message = langManager.getComponent(String.valueOf(sender instanceof Player p ? p : null),
                "commands." + COMMAND_NAME + ".usage",
                "<yellow>Usage:\n" +
                        "<white>/" + label + " give <player> <amount>\n" +
                        "<white>/" + label + " take <player> <amount>\n" +
                        "<white>/" + label + " set <player> <amount>\n" +
                        "<white>/" + label + " reset <player>");
        sender.sendMessage(message);
    }


    private void handleGive(CommandSender sender, OfflinePlayer target, String[] args, String label) {
        if (args.length < 3) {
            Component message = langManager.getComponent(String.valueOf(sender instanceof Player p ? p : null),
                    "commands." + COMMAND_NAME + ".give-usage",
                    "<yellow>Usage: <white>{usage}",
                    ComponentPlaceholder.of("{usage}", "/" + label + " give <player> <amount>"));
            sender.sendMessage(message);
            return;
        }

        double amount = parseAmount(args[2], sender);
        if (amount < 0) return;

        if (target instanceof Player onlineTarget) {
            EconomyResponse response = economyManager.deposit(onlineTarget, amount);

            if (response.success()) {
                Component message = langManager.getComponent(String.valueOf(sender instanceof Player p ? p : null),
                        "commands." + COMMAND_NAME + ".give-success",
                        "<green>Gave <white>{amount} <green>to <white>{player}",
                        ComponentPlaceholder.of("{amount}", economyManager.format(amount)),
                        ComponentPlaceholder.of("{player}", target.getName()));
                sender.sendMessage(message);

                if (!sender.equals(target)) {
                    onlineTarget.sendMessage(langManager.getComponent(String.valueOf(onlineTarget),
                            "commands." + COMMAND_NAME + ".give-received",
                            "<green>You received <white>{amount}",
                            ComponentPlaceholder.of("{amount}", economyManager.format(amount))));
                }

                plugin.getLogger().info(String.format("[Eco] %s gave %s %.2f to %s",
                        sender.getName(), economyManager.getEconomyName(), amount, target.getName()));
            } else {
                Component message = langManager.getComponent(String.valueOf(sender instanceof Player p ? p : null),
                        "commands." + COMMAND_NAME + ".transaction-failed",
                        "<red>Transaction failed: {error}",
                        ComponentPlaceholder.of("{error}", response.errorMessage));
                sender.sendMessage(message);

                plugin.getLogger().warning(String.format("[Eco] Give failed for %s: %s",
                        target.getName(), response.errorMessage));
            }
        } else {
            sender.sendMessage(langManager.getComponent(String.valueOf(sender instanceof Player p ? p : null),
                    "commands." + COMMAND_NAME + ".offline-player",
                    "<red>Cannot modify balance of offline players."));
        }
    }

    private void handleTake(CommandSender sender, OfflinePlayer target, String[] args, String label) {
        if (args.length < 3) {
            Component message = langManager.getComponent(String.valueOf(sender instanceof Player p ? p : null),
                    "commands." + COMMAND_NAME + ".take-usage",
                    "<yellow>Usage: <white>{usage}",
                    ComponentPlaceholder.of("{usage}", "/" + label + " take <player> <amount>"));
            sender.sendMessage(message);
            return;
        }

        double amount = parseAmount(args[2], sender);
        if (amount < 0) return;

        if (target instanceof Player onlineTarget) {
            EconomyResponse response = economyManager.withdraw(onlineTarget, amount);

            if (response.success()) {
                Component message = langManager.getComponent(String.valueOf(sender instanceof Player p ? p : null),
                        "commands." + COMMAND_NAME + ".take-success",
                        "<green>Took <white>{amount} <green>from <white>{player}",
                        ComponentPlaceholder.of("{amount}", economyManager.format(amount)),
                        ComponentPlaceholder.of("{player}", target.getName()));
                sender.sendMessage(message);

                if (!sender.equals(target)) {
                    onlineTarget.sendMessage(langManager.getComponent(String.valueOf(onlineTarget),
                            "commands." + COMMAND_NAME + ".take-taken",
                            "<red>{amount} <gray>was taken from your balance",
                            ComponentPlaceholder.of("{amount}", economyManager.format(amount))));
                }

                plugin.getLogger().info(String.format("[Eco] %s took %s %.2f from %s",
                        sender.getName(), economyManager.getEconomyName(), amount, target.getName()));
            } else {
                Component message = langManager.getComponent(String.valueOf(sender instanceof Player p ? p : null),
                        "commands." + COMMAND_NAME + ".transaction-failed",
                        "<red>Transaction failed: {error}",
                        ComponentPlaceholder.of("{error}", response.errorMessage));
                sender.sendMessage(message);
            }
        } else {
            sender.sendMessage(langManager.getComponent(String.valueOf(sender instanceof Player p ? p : null),
                    "commands." + COMMAND_NAME + ".offline-player",
                    "<red>Cannot modify balance of offline players."));
        }
    }


    private void handleSet(CommandSender sender, OfflinePlayer target, String[] args, String label) {
        if (args.length < 3) {
            Component message = langManager.getComponent(String.valueOf(sender instanceof Player p ? p : null),
                    "commands." + COMMAND_NAME + ".set-usage",
                    "<yellow>Usage: <white>{usage}",
                    ComponentPlaceholder.of("{usage}", "/" + label + " set <player> <amount>"));
            sender.sendMessage(message);
            return;
        }

        double amount = parseAmount(args[2], sender);
        if (amount < 0) return;

        if (target instanceof Player onlineTarget) {
            double currentBalance = economyManager.getBalance(onlineTarget);

            if (amount > currentBalance) {
                double giveAmount = amount - currentBalance;
                EconomyResponse response = economyManager.deposit(onlineTarget, giveAmount);

                if (response.success()) {
                    sendSetSuccessMessage(sender, onlineTarget, amount);
                } else {
                    sendTransactionFailedMessage(sender, response.errorMessage);
                }
            } else if (amount < currentBalance) {
                double takeAmount = currentBalance - amount;
                EconomyResponse response = economyManager.withdraw(onlineTarget, takeAmount);

                if (response.success()) {
                    sendSetSuccessMessage(sender, onlineTarget, amount);
                } else {
                    sendTransactionFailedMessage(sender, response.errorMessage);
                }
            } else {
                sender.sendMessage(langManager.getComponent(String.valueOf(sender instanceof Player p ? p : null),
                        "commands." + COMMAND_NAME + ".set-success",
                        "<green>Set balance for <white>{player} <green>to <white>{amount}",
                        ComponentPlaceholder.of("{player}", target.getName()),
                        ComponentPlaceholder.of("{amount}", economyManager.format(amount))));
            }
        } else {
            sender.sendMessage(langManager.getComponent(String.valueOf(sender instanceof Player p ? p : null),
                    "commands." + COMMAND_NAME + ".offline-player",
                    "<red>Cannot modify balance of offline players."));
        }
    }


    private void handleReset(CommandSender sender, OfflinePlayer target) {
        if (target instanceof Player onlineTarget) {
            double bal = economyManager.getBalance(onlineTarget);

            if (bal > 0) {
                EconomyResponse response = economyManager.withdraw(onlineTarget, bal);

                if (response.success()) {
                    Component message = langManager.getComponent(String.valueOf(sender instanceof Player p ? p : null),
                            "commands." + COMMAND_NAME + ".reset-success",
                            "<green>Reset balance for <white>{player}",
                            ComponentPlaceholder.of("{player}", target.getName()));
                    sender.sendMessage(message);

                    plugin.getLogger().info(String.format("[Eco] %s reset balance for %s",
                            sender.getName(), target.getName()));
                } else {
                    Component message = langManager.getComponent(String.valueOf(sender instanceof Player p ? p : null),
                            "commands." + COMMAND_NAME + ".transaction-failed",
                            "<red>Transaction failed: {error}",
                            ComponentPlaceholder.of("{error}", response.errorMessage));
                    sender.sendMessage(message);
                }
            } else {
                sender.sendMessage(langManager.getComponent(String.valueOf(sender instanceof Player p ? p : null),
                        "commands." + COMMAND_NAME + ".reset-already-zero",
                        "<gray>Balance for <white>{player} <gray>is already zero",
                        ComponentPlaceholder.of("{player}", target.getName())));
            }
        } else {
            sender.sendMessage(langManager.getComponent(String.valueOf(sender instanceof Player p ? p : null),
                    "commands." + COMMAND_NAME + ".offline-player",
                    "<red>Cannot modify balance of offline players."));
        }
    }

    private void sendSetSuccessMessage(CommandSender sender, Player target, double amount) {
        Component message = langManager.getComponent(String.valueOf(sender instanceof Player p ? p : null),
                "commands." + COMMAND_NAME + ".set-success",
                "<green>Set balance for <white>{player} <green>to <white>{amount}",
                ComponentPlaceholder.of("{player}", target.getName()),
                ComponentPlaceholder.of("{amount}", economyManager.format(amount)));
        sender.sendMessage(message);

        if (!sender.equals(target)) {
            target.sendMessage(langManager.getComponent(String.valueOf(target),
                    "commands." + COMMAND_NAME + ".set-received",
                    "<green>Your balance was set to <white>{amount}",
                    ComponentPlaceholder.of("{amount}", economyManager.format(amount))));
        }
    }

    private void sendTransactionFailedMessage(CommandSender sender, String errorMessage) {
        Component message = langManager.getComponent(String.valueOf(sender instanceof Player p ? p : null),
                "commands." + COMMAND_NAME + ".transaction-failed",
                "<red>Transaction failed: {error}",
                ComponentPlaceholder.of("{error}", errorMessage));
        sender.sendMessage(message);
    }


    private double parseAmount(String input, CommandSender sender) {
        try {
            double amount = Double.parseDouble(input);
            if (amount < 0) {
                sender.sendMessage(langManager.getComponent(String.valueOf(sender instanceof Player p ? p : null),
                        "commands." + COMMAND_NAME + ".negative-amount",
                        "<red>Amount cannot be negative!"));
                return -1;
            }
            if (amount > config.maxTransactionAmount) {
                sender.sendMessage(langManager.getComponent(String.valueOf(sender instanceof Player p ? p : null),
                        "commands." + COMMAND_NAME + ".amount-too-high",
                        "<red>Amount exceeds maximum allowed: {max}",
                        ComponentPlaceholder.of("{max}", economyManager.format(config.maxTransactionAmount))));
                return -1;
            }
            return amount;
        } catch (NumberFormatException e) {
            Component message = langManager.getComponent(String.valueOf(sender instanceof Player p ? p : null),
                    "commands." + COMMAND_NAME + ".invalid-amount",
                    "<red>Invalid amount: <white>{amount}",
                    ComponentPlaceholder.of("{amount}", input));
            sender.sendMessage(message);
            return -1;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            return List.of();
        }

        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            for (String s : List.of("give", "take", "set", "reset")) {
                if (s.startsWith(input)) completions.add(s);
            }
        } else if (args.length == 2) {
            String input = args[1].toLowerCase();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(input)) {
                    completions.add(p.getName());
                }
            }
        }
        return completions;
    }
}