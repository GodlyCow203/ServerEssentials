package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.config.EcoConfig;
import net.godlycow.org.economy.eco.EconomyManager;
import net.godlycow.org.economy.eco.EconomyResponse;
import net.godlycow.org.language.PlayerLanguageManager;
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
    private static final String PERMISSION = "essc.command.eco";

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
        Player player = sender instanceof Player ? (Player) sender : null;

        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(langManager.getMessageFor(player, "commands.eco.no-permission",
                    "<#FF2424>You need permission <#c0f0ff>{permission}<#FF2424>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        if (!economyManager.isEnabled()) {
            sender.sendMessage(langManager.getMessageFor(player, "commands.eco.no-economy",
                    "<#FF2424>Economy system is not available!"));
            return true;
        }

        if (args.length < 2) {
            sendUsageMessage(sender, label, player);
            return true;
        }

        OfflinePlayer target = getTargetPlayer(args[1]);
        if (target.getName() == null) {
            sender.sendMessage(langManager.getMessageFor(player, "commands.eco.player-not-found",
                    "<#FF2424>Player not found: <#c0f0ff>{player}",
                    ComponentPlaceholder.of("{player}", args[1])));
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "give" -> handleGive(sender, player, target, args, label);
            case "take" -> handleTake(sender, player, target, args, label);
            case "reset" -> handleReset(sender, player, target);
            case "set" -> handleSet(sender, player, target, args, label);
            default -> sender.sendMessage(langManager.getMessageFor(player, "commands.eco.unknown-subcommand",
                    "<#FF2424>Unknown subcommand: <#c0f0ff>{sub}\n<#708090>Available: give, take, set, reset",
                    ComponentPlaceholder.of("{sub}", sub)));
        }

        return true;
    }

    private void sendUsageMessage(CommandSender sender, String label, Player player) {
        sender.sendMessage(langManager.getMessageFor(player, "commands.eco.usage",
                "<#279CF5>Usage: <white>/" + label + " give <player> <amount>\n" +
                        "<white>/" + label + " take <player> <amount>\n" +
                        "<white>/" + label + " set <player> <amount>\n" +
                        "<white>/" + label + " reset <player>"));
    }

    private void handleGive(CommandSender sender, Player player, OfflinePlayer target, String[] args, String label) {
        if (args.length < 3) {
            sender.sendMessage(langManager.getMessageFor(player, "commands.eco.give-usage",
                    "<#279CF5>Usage: <white>{usage}",
                    ComponentPlaceholder.of("{usage}", "/" + label + " give <player> <amount>")));
            return;
        }

        double amount = parseAmount(args[2], sender, player);
        if (amount < 0) return;

        if (target instanceof Player onlineTarget) {
            EconomyResponse response = economyManager.deposit(onlineTarget, amount);

            if (response.success()) {
                sender.sendMessage(langManager.getMessageFor(player, "commands.eco.give-success",
                        "<#279CF5>Gave <#c0f0ff>{amount} <#279CF5>to <#c0f0ff>{player}",
                        ComponentPlaceholder.of("{amount}", economyManager.format(amount)),
                        ComponentPlaceholder.of("{player}", target.getName())));

                if (!sender.equals(target)) {
                    onlineTarget.sendMessage(langManager.getMessageFor(onlineTarget, "commands.eco.give-received",
                            "<#279CF5>You received <#c0f0ff>{amount}",
                            ComponentPlaceholder.of("{amount}", economyManager.format(amount))));
                }

                plugin.getLogger().info(String.format("[Eco] %s gave %s %.2f to %s",
                        sender.getName(), economyManager.getEconomyName(), amount, target.getName()));
            } else {
                sendTransactionFailedMessage(sender, player, response.errorMessage);
            }
        } else {
            sender.sendMessage(langManager.getMessageFor(player, "commands.eco.offline-player",
                    "<#FF2424>Cannot modify balance of offline players."));
        }
    }

    private void handleTake(CommandSender sender, Player player, OfflinePlayer target, String[] args, String label) {
        if (args.length < 3) {
            sender.sendMessage(langManager.getMessageFor(player, "commands.eco.take-usage",
                    "<#279CF5>Usage: <white>{usage}",
                    ComponentPlaceholder.of("{usage}", "/" + label + " take <player> <amount>")));
            return;
        }

        double amount = parseAmount(args[2], sender, player);
        if (amount < 0) return;

        if (target instanceof Player onlineTarget) {
            EconomyResponse response = economyManager.withdraw(onlineTarget, amount);

            if (response.success()) {
                sender.sendMessage(langManager.getMessageFor(player, "commands.eco.take-success",
                        "<#FF2424>Took <#c0f0ff>{amount} <#FF2424>from <#c0f0ff>{player}",
                        ComponentPlaceholder.of("{amount}", economyManager.format(amount)),
                        ComponentPlaceholder.of("{player}", target.getName())));

                if (!sender.equals(target)) {
                    onlineTarget.sendMessage(langManager.getMessageFor(onlineTarget, "commands.eco.take-taken",
                            "<#FF2424>{amount} <#708090>was taken from your balance",
                            ComponentPlaceholder.of("{amount}", economyManager.format(amount))));
                }

                plugin.getLogger().info(String.format("[Eco] %s took %s %.2f from %s",
                        sender.getName(), economyManager.getEconomyName(), amount, target.getName()));
            } else {
                sendTransactionFailedMessage(sender, player, response.errorMessage);
            }
        } else {
            sender.sendMessage(langManager.getMessageFor(player, "commands.eco.offline-player",
                    "<#FF2424>Cannot modify balance of offline players."));
        }
    }

    private void handleSet(CommandSender sender, Player player, OfflinePlayer target, String[] args, String label) {
        if (args.length < 3) {
            sender.sendMessage(langManager.getMessageFor(player, "commands.eco.set-usage",
                    "<#279CF5>Usage: <white>{usage}",
                    ComponentPlaceholder.of("{usage}", "/" + label + " set <player> <amount>")));
            return;
        }

        double amount = parseAmount(args[2], sender, player);
        if (amount < 0) return;

        if (target instanceof Player onlineTarget) {
            double currentBalance = economyManager.getBalance(onlineTarget);

            if (amount > currentBalance) {
                EconomyResponse response = economyManager.deposit(onlineTarget, amount - currentBalance);
                if (response.success()) sendSetSuccessMessage(sender, player, onlineTarget, amount);
                else sendTransactionFailedMessage(sender, player, response.errorMessage);
            } else if (amount < currentBalance) {
                EconomyResponse response = economyManager.withdraw(onlineTarget, currentBalance - amount);
                if (response.success()) sendSetSuccessMessage(sender, player, onlineTarget, amount);
                else sendTransactionFailedMessage(sender, player, response.errorMessage);
            } else {
                sender.sendMessage(langManager.getMessageFor(player, "commands.eco.set-success",
                        "<#279CF5>Set balance for <#c0f0ff>{player} <#279CF5>to <#c0f0ff>{amount}",
                        ComponentPlaceholder.of("{player}", target.getName()),
                        ComponentPlaceholder.of("{amount}", economyManager.format(amount))));
            }
        } else {
            sender.sendMessage(langManager.getMessageFor(player, "commands.eco.offline-player",
                    "<#FF2424>Cannot modify balance of offline players."));
        }
    }

    private void handleReset(CommandSender sender, Player player, OfflinePlayer target) {
        if (target instanceof Player onlineTarget) {
            double bal = economyManager.getBalance(onlineTarget);

            if (bal > 0) {
                EconomyResponse response = economyManager.withdraw(onlineTarget, bal);
                if (response.success()) {
                    sender.sendMessage(langManager.getMessageFor(player, "commands.eco.reset-success",
                            "<#279CF5>Reset balance for <#c0f0ff>{player}",
                            ComponentPlaceholder.of("{player}", target.getName())));

                    plugin.getLogger().info(String.format("[Eco] %s reset balance for %s",
                            sender.getName(), target.getName()));
                } else {
                    sendTransactionFailedMessage(sender, player, response.errorMessage);
                }
            } else {
                sender.sendMessage(langManager.getMessageFor(player, "commands.eco.reset-already-zero",
                        "<#708090>Balance for <#c0f0ff>{player} <#708090>is already zero",
                        ComponentPlaceholder.of("{player}", target.getName())));
            }
        } else {
            sender.sendMessage(langManager.getMessageFor(player, "commands.eco.offline-player",
                    "<#FF2424>Cannot modify balance of offline players."));
        }
    }

    private void sendSetSuccessMessage(CommandSender sender, Player senderPlayer, Player target, double amount) {
        sender.sendMessage(langManager.getMessageFor(senderPlayer, "commands.eco.set-success",
                "<#279CF5>Set balance for <#c0f0ff>{player} <#279CF5>to <#c0f0ff>{amount}",
                ComponentPlaceholder.of("{player}", target.getName()),
                ComponentPlaceholder.of("{amount}", economyManager.format(amount))));

        if (!sender.equals(target)) {
            target.sendMessage(langManager.getMessageFor(target, "commands.eco.set-received",
                    "<#279CF5>Your balance was set to <#c0f0ff>{amount}",
                    ComponentPlaceholder.of("{amount}", economyManager.format(amount))));
        }
    }

    private void sendTransactionFailedMessage(CommandSender sender, Player player, String errorMessage) {
        sender.sendMessage(langManager.getMessageFor(player, "commands.eco.transaction-failed",
                "<#FF2424>Transaction failed: {error}",
                ComponentPlaceholder.of("{error}", errorMessage)));
    }

    private double parseAmount(String input, CommandSender sender, Player player) {
        try {
            double amount = Double.parseDouble(input);
            if (amount < 0) {
                sender.sendMessage(langManager.getMessageFor(player, "commands.eco.negative-amount",
                        "<#FF2424>Amount cannot be negative!"));
                return -1;
            }
            if (amount > config.maxTransactionAmount) {
                sender.sendMessage(langManager.getMessageFor(player, "commands.eco.amount-too-high",
                        "<#FF2424>Amount exceeds maximum allowed: {max}",
                        ComponentPlaceholder.of("{max}", economyManager.format(config.maxTransactionAmount))));
                return -1;
            }
            return amount;
        } catch (NumberFormatException e) {
            sender.sendMessage(langManager.getMessageFor(player, "commands.eco.invalid-amount",
                    "<#FF2424>Invalid amount: <#c0f0ff>{amount}",
                    ComponentPlaceholder.of("{amount}", input)));
            return -1;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(PERMISSION)) return List.of();

        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            for (String s : List.of("give", "take", "set", "reset")) {
                if (s.startsWith(input)) completions.add(s);
            }
        } else if (args.length == 2) {
            String input = args[1].toLowerCase();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(input)) completions.add(p.getName());
            }
        }
        return completions;
    }
}