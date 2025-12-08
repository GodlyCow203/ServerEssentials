package net.lunark.io.commands.impl;

import net.lunark.io.commands.config.EcoConfig;
import net.lunark.io.economy.ServerEssentialsEconomy;
import net.lunark.io.language.PlayerLanguageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static net.lunark.io.language.LanguageManager.ComponentPlaceholder;

public final class EcoCommand implements CommandExecutor, TabCompleter {
     private static final String PERMISSION = "serveressentials.command.eco";
    private static final String COMMAND_NAME = "eco";

    private final PlayerLanguageManager langManager;
    private final EcoConfig config;
    private final ServerEssentialsEconomy economy;

    public EcoCommand(PlayerLanguageManager langManager, EcoConfig config, ServerEssentialsEconomy economy) {
        this.langManager = langManager;
        this.config = config;
        this.economy = economy;
    }

    private OfflinePlayer getTargetPlayer(String name) {
        Player online = Bukkit.getPlayer(name);
        return online != null ? online : Bukkit.getOfflinePlayerIfCached(name);
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

        if (args.length < 2) {
            Component message = langManager.getComponent(String.valueOf(sender instanceof Player p ? p : null),
                    "commands." + COMMAND_NAME + ".usage",
                    "<yellow>Usage: <white>{usage}",
                    ComponentPlaceholder.of("{usage}", "/eco <give|take|reset> <player> [amount]"));
            sender.sendMessage(message);
            return true;
        }

        OfflinePlayer target = getTargetPlayer(args[1]);
        if (target == null) {
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
            default -> {
                Component message = langManager.getComponent(String.valueOf(sender instanceof Player p ? p : null),
                        "commands." + COMMAND_NAME + ".unknown-subcommand",
                        "<red>Unknown subcommand: <white>{sub}",
                        ComponentPlaceholder.of("{sub}", sub));
                sender.sendMessage(message);
            }
        }

        return true;
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

        economy.depositPlayer(target, amount);
        Component message = langManager.getComponent(String.valueOf(sender instanceof Player p ? p : null),
                "commands." + COMMAND_NAME + ".give-success",
                "<green>Gave <white>{amount} <green>to <white>{player}",
                ComponentPlaceholder.of("{amount}", economy.format(amount)),
                ComponentPlaceholder.of("{player}", target.getName()));
        sender.sendMessage(message);
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

        economy.withdrawPlayer(target, amount);
        Component message = langManager.getComponent(String.valueOf(sender instanceof Player p ? p : null),
                "commands." + COMMAND_NAME + ".take-success",
                "<green>Took <white>{amount} <green>from <white>{player}",
                ComponentPlaceholder.of("{amount}", economy.format(amount)),
                ComponentPlaceholder.of("{player}", target.getName()));
        sender.sendMessage(message);
    }

    private void handleReset(CommandSender sender, OfflinePlayer target) {
        double bal = economy.getBalance(target);
        economy.withdrawPlayer(target, bal);
        Component message = langManager.getComponent(String.valueOf(sender instanceof Player p ? p : null),
                "commands." + COMMAND_NAME + ".reset-success",
                "<green>Reset balance for <white>{player}",
                ComponentPlaceholder.of("{player}", target.getName()));
        sender.sendMessage(message);
    }

    private double parseAmount(String input, CommandSender sender) {
        try {
            return Double.parseDouble(input);
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
            for (String s : List.of("give", "take", "reset")) {
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